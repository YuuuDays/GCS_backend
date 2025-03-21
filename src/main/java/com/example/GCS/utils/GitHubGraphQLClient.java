package com.example.GCS.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class GitHubGraphQLClient {

    private final WebClient webClient;
    private final String githubToken;
    private static final Logger logger = LoggerFactory.getLogger(GitHubGraphQLClient.class);

    // @Valueアノテーションはコンストラクタより後に読み込まれるため、引数にセット
    public GitHubGraphQLClient(WebClient.Builder webClientBuilder, @Value("${GITHUB_TOKEN}") String githubToken) {
        this.githubToken = githubToken; // ここで直接受け取る
        logger.debug("★githubToken ="+githubToken);
        this.webClient = webClientBuilder
                .baseUrl("https://api.github.com/graphql")  // GitHub の GraphQL エンドポイント
                .defaultHeader("Authorization", "Bearer " + this.githubToken)  // 認証トークン
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
        // クエリ文字列
        String gqlQuery = """
        query ($login: String!) {
          user(login: $login) {
            contributionsCollection {
              contributionCalendar {
                weeks {
                  contributionDays {
                    date
                    contributionCount
                  }
                }
              }
            }
          }
        }
    """;

        // 送信するリクエストボディ（変数を含む）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", gqlQuery);

        Map<String, Object> variables = new HashMap<>();
        variables.put("login", username);
        requestBody.put("variables", variables);

        return webClient.post()
                .bodyValue(requestBody)  // JSON として送信
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    return response.bodyToMono(String.class).flatMap(errorBody -> {
                        logger.debug("GitHub API error response: " + errorBody);
                        return Mono.error(new RuntimeException("GitHub API error: " + errorBody));
                    });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})  // レスポンスを Mono<Map<String, Object>> に変換
                .doOnNext(response -> {
                    // 正常時にレスポンス内容をログ出力
//                    logger.debug("GitHub API successful response: " + response);
                });

    }
}
