package com.example.GCS.service;

import com.example.GCS.component.GitHubGraphQLClient;
import com.example.GCS.config.EnvConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.time.temporal.TemporalAdjusters;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

// 概要:githubのデータを取得するクラス
@Service
public class GithubService {

    private static final String GITHUB_EVENTS_API = "https://api.github.com/users/{username}/events";
    private static final String GITHUB_REPOS_API = "https://api.github.com/users/{username}/repos";


    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);
    private final ObjectMapper objectMapper;
    private final GitHubGraphQLClient gitHubGraphQLClient;
    private final String githubToken = EnvConfig.getGithubToken();

    public GithubService(ObjectMapper objectMapper, GitHubGraphQLClient gitHubGraphQLClient) {
        this.objectMapper = objectMapper;
        this.gitHubGraphQLClient = gitHubGraphQLClient;
    }

    /**
     * 概要 : フロントが/dashboardからログインした日時刻をLocalDateTimeで返す
     *
     * @param clientTimestamp フロントから送られるString型日時刻
     * @return LocalDateTime 概要通り、また変換失敗したらバックエンドで現在日時刻を返す
     */
    public LocalDateTime parseToLocalDateTime(String clientTimestamp) {
        try {
            String formattedTimestamp = clientTimestamp.split("\\.")[0];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            return LocalDateTime.parse(formattedTimestamp, formatter);
        } catch (DateTimeParseException e) {
            logger.warn("引数異常->\"" + clientTimestamp + "\"デフォルト値を使います");
            return LocalDateTime.now();  // デフォルトで現在時刻を返す
        }
    }

    /**
     * 概要 : ユーザのGithubNameを元にGitHubAPIにアクセスする
     *
     * @param username ...ユーザのGitHubName
     * @return String ...API取得後のHTTPレスポンスBody部分
     * 正常の場合 ユーザの全レポジトリを取得
     * 異常の場合　null
     */
    public String fetchGitHubData(String username) {
//        String githubToken = EnvConfig.getGithubToken();
        logger.debug("GITHUB_TOKEN:" + githubToken);

        /*-------------------------------------------------------------
         *  GitHub API のエンドポイント（リポジトリ一覧取得）ック
         *------------------------------------------------------------*/
        String url = "https://api.github.com/users/" + username + "/repos";
        // HTTP ヘッダー設定
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");  // Accept ヘッダーを正しく設定
        // HTTP エンティティ作成
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // HTTP リクエストを送信するためのクライアント
        RestTemplate restTemplate = new RestTemplate();
        // GitHub API からリポジトリ一覧を取得
        try {
            ResponseEntity<String> repoResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String responseBody = repoResponse.getBody();
            //logger.debug("★responseBody:" + responseBody);
            return responseBody;
        } catch (RestClientException e) {
            logger.error("Error:" + e.getMessage());
            return null;
        }
        // MEMO
        // リポジトリ内のすべてのAT_TIMEを抜き出しMAP<Stirng,String>にぶちこむ？あるいはString[]
        // それの最新を取得する。
        //　取得したら世界時間なので日本時間に直すために+9時間する
        //
    }

    // StringからJSONへ
    public JsonNode StringToJSON(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            // JSONのパースエラーを処理
            System.err.println("JSONのパースに失敗しました: " + e.getMessage());
            e.printStackTrace(); // スタックトレースを出力
        }
        return null; // エラーが発生した場合はnullを返す
    }

    /**
     * 概要 : GitHubAPIから取得したレポジトリ一覧から最後にコミットしたレポジトリを返す
     *
     * @param jsonNode ...レポジトリ一覧
     * @return JsonNode ...最後にコミットしたレポジトリを格納
     */
    public JsonNode getTheDayOfTheMostMomentCommit(JsonNode jsonNode) {
        // 最新のプッシュ日時を探す
        JsonNode latestRepo = null;
        Date latestPushDate = null;
        Date pushedAt = null;

        for (JsonNode repo : jsonNode) {
            String pushedAtStr = repo.get("pushed_at").asText();
            //logger.debug("★pushedAtStr:"+pushedAtStr);
            try {
                pushedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(pushedAtStr);
                //logger.debug("★pushedAt:"+pushedAt);
            } catch (Exception e) {
                logger.error("Data format conversion error:" + e.getMessage());
                return null;
            }

            // 最新のプッシュ日時のリポジトリを見つける
            if (latestPushDate == null || pushedAt.after(latestPushDate)) {
                latestPushDate = pushedAt;
                latestRepo = repo;
            }
        }
        return latestRepo;
    }

    /**
     * 概要 : 引数で受け取った(最新)レポジトリから今日のコミットがあるかどうか調べる
     *
     * @param jsonNode ...最新のレポジトリ
     * @return boolean ...今日のコミットが　有る(true)/無し(false)
     */
    public boolean getTodayCommit(JsonNode jsonNode) {
        Date pushedAt;
        Date isToday;
        String formattedDate;

        // 最新の日付を取得
        String pushedAtStr = jsonNode.get("pushed_at").asText();
        logger.debug("★レポジトリ単体から取得(UTC):" + pushedAtStr);
        try {
            // UTCの日付文字列をDateオブジェクトに変換
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            utcFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            pushedAt = utcFormat.parse(pushedAtStr);

            // 日本時間にフォーマット
            SimpleDateFormat jstFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            jstFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Tokyo"));
            formattedDate = jstFormat.format(pushedAt);

            logger.debug("★レポジトリ単体の時間を日本時間へ:" + formattedDate);

            // 与えられた日付をLocalDateに変換
            String datePart = formattedDate.substring(0, 10);
            String datePartAndFormat = datePart.replace("/", "-");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate inputDate = LocalDate.parse(datePartAndFormat, formatter);

            // 今日の日付を取得
            LocalDate today = LocalDate.now();
            logger.debug("★inputDate:★today:" + inputDate + ":" + today);

            // 日付を比較
            return inputDate.equals(today);

        } catch (Exception e) {
            logger.error("Data format conversion error:" + e.getMessage());
            return false;
        }
    }


    // 概要:一番最新のレポジトリの言語使用率取得
    public Map<String, Object> getLatestRepositoryLanguageRatio(JsonNode jsonNode) {
        Map<String, Object> response = new HashMap<>();
        String languagesUr = jsonNode.get("languages_url").asText();

        // HTTP ヘッダー設定
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        // HTTP エンティティ作成
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // HTTP リクエストを送信するためのクライアント
        RestTemplate restTemplate = new RestTemplate();
        // GitHub API からリポジトリ一覧を取得
        try {
            ResponseEntity<String> repoResponse = restTemplate.exchange(languagesUr, HttpMethod.GET, entity, String.class);
            String responseBody = repoResponse.getBody();

            // JSONに変換
            JsonNode responseJsonNode = StringToJSON(responseBody);

            // JsonNode を Map<String, Object> に変換
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> resultMap = objectMapper.convertValue(responseJsonNode, Map.class);

            logger.debug("★★resultMap:" + resultMap);
            return resultMap;
        } catch (RestClientException e) {
            logger.error("Error:" + e.getMessage());
            return null;
        }

    }

    //概要:一週間のコミット履歴
    public Mono<Boolean[]> getWeeklyCommitRateAsync(String userName) {
        // 非同期でGitContributeを取得するAPIへ接続
        //Mono<Map<String,Object>> response = gitHubGraphQLClient.fetchUserContributions(userName);

        // 非同期で結果を処理
        return gitHubGraphQLClient.fetchUserContributions(userName)
                .map(result -> processContributions(result)); // 非同期のまま処理
    }

    private Boolean[] processContributions(Map<String, Object> result) {
        Boolean[] contributionsWeek = new Boolean[7];
        logger.debug("★★★result:" + result);
        // 今日の日付を取得
        LocalDate today = LocalDate.now();
        //テスト用
//        String x = "2025-03-13";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        today = LocalDate.parse(x, formatter);

        // 今週の開始日 (日曜日)
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        logger.debug("startOfWeek" + startOfWeek);
        // 今週の終了日 (土曜日)
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        logger.debug("endOfWeek" + endOfWeek);

        try {
            List<Map<String, Object>> contributionDataList = Optional.ofNullable(result)
                    .map(d -> (Map<String, Object>) d.get("data"))
                    .map(d -> (Map<String, Object>) d.get("user"))
                    .map(d -> (Map<String, Object>) d.get("contributionsCollection"))
                    .map(d -> (Map<String, Object>) d.get("contributionCalendar"))
                    .map(d -> (List<Map<String, Object>>) d.get("weeks"))
                    .orElse(Collections.emptyList())
                    .stream()
                    .flatMap(week -> Optional.ofNullable((List<Map<String, Object>>) week.get("contributionDays"))
                            .orElse(Collections.emptyList())
                            .stream())
                    .filter(day -> {
//                    logger.debug("day is="+day);
                        LocalDate dayDate = LocalDate.parse((String) day.get("date"));
                        return !dayDate.isBefore(startOfWeek) && !dayDate.isAfter(endOfWeek);
                    })
                    .toList();

            if (!contributionDataList.isEmpty()) {
                contributionDataList.forEach(d -> {

                    LocalDate dayDate = LocalDate.parse((String) d.get("date"));
                    int contributionCount = (int) d.get("contributionCount");

                    // 対象の日付のインデックスを取得
                    int dayOfWeek = dayDate.getDayOfWeek().getValue() % 7; // 0 (日曜日) ～ 6 (土曜日)

                    // contributionCount が 1 以上なら Boolean 配列を更新
                    if (contributionCount > 0) {
                        contributionsWeek[dayOfWeek] = true;
                    } else {
                        contributionsWeek[dayOfWeek] = false;
                    }

                    logger.debug("今週の範囲 (" + startOfWeek + " ～ " + endOfWeek + ") のデータ: " + d);
                });
            } else {
                logger.debug("今週の範囲 (" + startOfWeek + " ～ " + endOfWeek + ") のデータは見つかりませんでした。");
            }

            return contributionsWeek;

        } catch (Exception e) {
            logger.debug("error:" + e.getMessage());
            return null;
        }

    }

}
