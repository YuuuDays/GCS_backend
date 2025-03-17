package com.example.GCS.controller;


import com.example.GCS.model.User;
import com.example.GCS.service.AuthService;
import com.example.GCS.service.GithubService;
import com.example.GCS.service.UserService;
import com.example.GCS.utils.VerifyResponseBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

//概要:/dashBoardへ返すAPIデータ
@RestController
@RequestMapping("/github")
public class GithubController {

    private final AuthService authService;
    private final GithubService githubService;
    private final UserService userService;


    private static final Logger logger = LoggerFactory.getLogger(GithubController.class);

    public GithubController(AuthService authService, GithubService githubService, UserService userService) {
        this.authService = authService;
        this.githubService = githubService;
        this.userService = userService;
    }

    /**
     * 概要: JWT+ユーザー情報を取得しヴァリデーションチェック後、登録する
     *
     * @param JWTToken requestHeader内に格納されたJWT
     * @return フロントへJSON形式で送信される
     * {
     * "languageUsage": "(ユーザの公開レポジトリにあるすべての)言語使用率",
     * "isCommitToday": "今日のコミットの有無",
     * "weeklyCommitRate": "1週間のコミット率",
     * "monthlyCommitRate": "一ヶ月のコミット率",
     * }
     */
    @GetMapping("/userDate")
    public ResponseEntity<Map<String, Object>> getGitHubData(@RequestHeader("Authorization") String JWTToken,
                                                             @RequestParam String clientTimeStamp) {
        // response用
        Map<String, Object> LatestRepositoryLanguageRatio;


        /*-------------------------------------------------------------
         * JWTトークンの検証
         *------------------------------------------------------------*/
        VerifyResponseBuilder JWTResponseBuilder = authService.verifyToken(JWTToken);

        //成功判定
        if (!JWTResponseBuilder.getSuccess()) {
            // - HTTP Status 401（Unauthorized）を設定
            logger.debug("失敗->JWTResponseBuilder.build():" + JWTResponseBuilder.build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(JWTResponseBuilder.build());
        }

        /*-------------------------------------------------------------
         *  DBからgithubNameを取得
         *------------------------------------------------------------*/
        // JWTからFirebaseTokenを取得
        FirebaseToken firebaseToken = userService.verifyJWT(JWTToken);
        if (firebaseToken == null || firebaseToken.getUid() == null) {
            Map<String, Object> response = new VerifyResponseBuilder().success(false).addError("DBにデータが存在しません。ログインし直してください").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // FirebaseToken→uidからDBのuserデータを取得
        User user = userService.getPersonalInfomation(firebaseToken.getUid());
        if (user == null) {
            Map<String, Object> response = new VerifyResponseBuilder().success(false).addError("DBにデータが存在しません。ログインし直してください").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        /*-------------------------------------------------------------
         *   githubNameを元にgitHubAPIを叩く(同期)
         *------------------------------------------------------------*/
        String response = githubService.fetchGitHubData(user.getGitName());
        JsonNode jsonNode = githubService.StringToJSON(response);
        if (response.isEmpty() || jsonNode.isEmpty()) {
            Map<String, Object> responseAPIError = new VerifyResponseBuilder().success(false).addError("データ取得に失敗しました、時間をおいてアクセスを試してください").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseAPIError);
        }
        /*-------------------------------------------------------------
         *  1週間文のレポジトリ取得(非同期)
         *------------------------------------------------------------*/
        //boolean[] weeklyCommitRate = githubService.getWeeklyCommitRate(user.getGitName());

        DeferredResult<Boolean[]> result = new DeferredResult<>();

        githubService.getWeeklyCommitRateAsync(user.getGitName())
                .doOnSuccess(contributions -> logger.debug("★ サービスからの非同期結果: " + Arrays.toString(contributions)))
                .doOnError(error -> logger.error("★ 非同期処理でエラー発生", error))
                .subscribe(result::setResult, result::setErrorResult);
        /*-------------------------------------------------------------
         *   (一番最新の)コミットされたリポジトリを取得
         *------------------------------------------------------------*/
        JsonNode latestCommitRepository = githubService.getTheDayOfTheMostMomentCommit(jsonNode);
        logger.debug("latestCommitRepository:" + latestCommitRepository);

        /*-------------------------------------------------------------
         *   今日のコミットがあるかどうか
         *------------------------------------------------------------*/
        boolean isTodayCommit = githubService.getTodayCommit(latestCommitRepository);
        logger.debug("今日のコミットの結果=" + isTodayCommit);

        /*-------------------------------------------------------------
         *   最新リポジトリの言語使用率を取得
         *------------------------------------------------------------*/
        LatestRepositoryLanguageRatio = githubService.getLatestRepositoryLanguageRatio(latestCommitRepository);
        // レスポンスデータを返す


        return ResponseEntity.ok().build();
    }
}
