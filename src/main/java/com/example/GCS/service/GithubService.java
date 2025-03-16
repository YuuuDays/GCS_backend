package com.example.GCS.service;

import com.example.GCS.config.EnvConfig;
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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
     * @param   clientTimestamp フロントから送られるString型日時刻
     * @return  LocalDateTime 概要通り、また変換失敗したらバックエンドで現在日時刻を返す
     */
    public LocalDateTime parseToLocalDateTime(String clientTimestamp)
    {
        try {
            String formattedTimestamp = clientTimestamp.split("\\.")[0];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            return LocalDateTime.parse(formattedTimestamp, formatter);
        } catch (DateTimeParseException e) {
            logger.warn("引数異常->\"" + clientTimestamp + "\"デフォルト値を使います");
            return LocalDateTime.now();  // デフォルトで現在時刻を返す
        }
    }

    // GitHub APIから情報取得
    public ResponseEntity<String> fetchGitHubData(String username) {
        String githubToken = EnvConfig.getGithubToken();
        logger.debug("GITHUB_TOKEN:"+githubToken);

        /*-------------------------------------------------------------
         *   ユーザーのイベント情報からコミット状況をチェック
         *------------------------------------------------------------*/
        String url = "https://api.github.com/users/" + username + "/events";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // HTTP リクエストを送信するためのクライアント
        RestTemplate restTemplate = new RestTemplate();
        // GitHub API からイベント情報を取得
        ResponseEntity<String> eventResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        logger.debug("★eventResponse:"+eventResponse);
        return eventResponse;
    }

    //概要:一週間のコミット履歴
    public boolean[] getWeeklyCommitRate(ResponseEntity<String> eventResponse)
    {
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
    public boolean[] getMonthlyCommitRate(ResponseEntity<String> eventResponse)
    {
        boolean[] monthlyCommitRate = new boolean[30];
        return monthlyCommitRate;
    }

}
