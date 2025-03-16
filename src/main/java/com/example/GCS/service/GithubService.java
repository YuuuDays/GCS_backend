package com.example.GCS.service;

import com.example.GCS.config.EnvConfig;
import com.example.GCS.utils.ResponseBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

// 概要:githubのデータを取得するクラス
@Service
public class GithubService {

    private static final String GITHUB_EVENTS_API = "https://api.github.com/users/{username}/events";
    private static final String GITHUB_REPOS_API = "https://api.github.com/users/{username}/repos";


    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    public GithubService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
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
        String githubToken = EnvConfig.getGithubToken();
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

    // 概要:最後にコミットされたレポジトリの日付
    public JsonNode getTheDayofTheMostMomentCommit(JsonNode jsonNode)
    {
        // 最新のプッシュ日時を探す
        JsonNode latestRepo = null;
        Date latestPushDate = null;
        Date pushedAt       = null;

        for (JsonNode repo : jsonNode) {
            String pushedAtStr = repo.get("pushed_at").asText();
            try {
                pushedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(pushedAtStr);

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

    // 概要:一番最新のレポジトリの言語使用率取得
//    public Map<String, Object> getLatestRepositoryLanguageRatio(JsonNode jsonNode) {
//
//    }

    //概要:一週間のコミット履歴
    public boolean[] getWeeklyCommitRate(ResponseEntity<String> eventResponse) {
        boolean[] commitDays = new boolean[7];
        if (eventResponse.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode events = objectMapper.readTree(eventResponse.getBody());
                LocalDate today = LocalDate.now();

                for (JsonNode event : events) {
                    if ("PushEvent".equals(event.get("type").asText())) {
                        LocalDate commitDate = LocalDate.parse(event.get("created_at").asText().substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);

                        // 過去7日間のコミットがあったかどうかをチェック
                        int daysAgo = (int) ChronoUnit.DAYS.between(commitDate, today);
                        if (daysAgo >= 0 && daysAgo < 7) {
                            commitDays[daysAgo] = true; // 該当の日にコミットがあった場合、trueに設定
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.error("Failed to fetch events: " + eventResponse.getStatusCode());
        }
        // 結果をMapに格納
//        result.put("commitDays", commitDays);
        return commitDays;
    }

    //概要:1カ月のコミット履歴
    public boolean[] getMonthlyCommitRate(ResponseEntity<String> eventResponse) {
        boolean[] monthlyCommitRate = new boolean[30];
        return monthlyCommitRate;
    }

}
