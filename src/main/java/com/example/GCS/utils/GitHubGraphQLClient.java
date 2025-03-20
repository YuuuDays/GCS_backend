package com.example.GCS.utils;

import com.example.GCS.config.EnvConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GitHubGraphQLClient {

    private final WebClient webClient;

    public GitHubGraphQLClient(WebClient.Builder webClientBuilder) {
        String githubToken = EnvConfig.getGithubToken();
        this.webClient = webClientBuilder
                .baseUrl("https://api.github.com/graphql")  // GitHub の GraphQL エンドポイント
                .defaultHeader("Authorization", "Bearer " + githubToken)  // 認証トークン
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/vnd.github.v4+json") // GitHubの推奨ヘッダー
                .build();
    }

    /**
     * 概要: userNameを元にしたContribute
     *        例:{contributionDays=[{date=2025-02-23,contributionCount=0},
     *                             {date=2025-02-24,contributionCount=0},...]
     *         補足:週の日曜日を基準として取得
     * @param username userのgitHubName名
     * @return Mono\<Map\<String,Object\>> 非同期で取得したAPIのデータをMap<String,Object>の形で返す
     * */
    public Mono<Map<String,Object>> fetchUserContributions(String username) {
        String query = "{ \"query\": \"query { user(login: \\\"" + username + "\\\") { contributionsCollection { contributionCalendar { weeks { contributionDays { date contributionCount } } } } } }\" }";

        return webClient.post()
                .bodyValue(query)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});// 非同期で Map に変換
    }
}
