package com.example.GCS.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import java.time.format.DateTimeParseException;
import java.util.*;

// 概要:githubのデータを取得するクラス
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
    public Map<String, Object> fetchGitHubData(String username) {
        Map<String, Object> result = new HashMap<>();

        boolean hasCommittedToday = false;
        boolean hasCommittedLastWeek = false;
        Set<String> languages = new HashSet<>();

        // ① ユーザーのイベント情報からコミット状況をチェック
        //レスポンスのボディを String 型で取得↓
        ResponseEntity<String> eventResponse = restTemplate.getForEntity(GITHUB_EVENTS_API, String.class, username);
        if (eventResponse.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode events = objectMapper.readTree(eventResponse.getBody());
                LocalDate today = LocalDate.now();
                LocalDate lastWeek = today.minusDays(7);

                for (JsonNode event : events) {
                    if ("PushEvent".equals(event.get("type").asText())) {
                        LocalDate commitDate = LocalDate.parse(event.get("created_at").asText().substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
                        if (commitDate.equals(today)) {
                            hasCommittedToday = true;
                        }
                        if (!commitDate.isBefore(lastWeek)) {
                            hasCommittedLastWeek = true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ② 公開リポジトリの言語を取得
        ResponseEntity<String> repoResponse = restTemplate.getForEntity(GITHUB_REPOS_API, String.class, username);
        if (repoResponse.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode repos = objectMapper.readTree(repoResponse.getBody());
                for (JsonNode repo : repos) {
                    String language = repo.get("language").asText();
                    if (language != null && !language.equals("null")) {
                        languages.add(language);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        result.put("hasCommittedToday", hasCommittedToday);
        result.put("hasCommittedLastWeek", hasCommittedLastWeek);
        result.put("languages", languages);
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

}
