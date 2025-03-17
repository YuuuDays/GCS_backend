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
import java.util.HashMap;
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
     * 概要: JWT検証後、非同期で値を取得
     * @param JWTToken requestHeader内に格納されたJWT
     * @return フロントへJSON形式で送信される
     * {
     * "isCommitToday": "今日のコミットの有無",
     * "languageUsage": "(ユーザの公開レポジトリにあるすべての)言語使用率",
     * "weeklyCommitRate": "1週間のコミット率",
     * }
     */
    @GetMapping("/userDate")
    public DeferredResult<ResponseEntity<Map<String, Object>>> getGitHubData(@RequestHeader("Authorization") String JWTToken,
                                                                             @RequestParam String clientTimeStamp) {
        // 非同期レスポンス用の DeferredResult を作成
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = new DeferredResult<>();

        //　レスポンス用の Map を作成
        Map<String, Object> responseUserRepositoryDate = new HashMap<>();

        /*-------------------------------------------------------------
         * JWTトークンの検証
         *------------------------------------------------------------*/
        VerifyResponseBuilder JWTResponseBuilder = authService.verifyToken(JWTToken);

        if (!JWTResponseBuilder.getSuccess()) {
            logger.debug("失敗->JWTResponseBuilder.build():" + JWTResponseBuilder.build());
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(JWTResponseBuilder.build()));
            return deferredResult;
        }

        /*-------------------------------------------------------------
         * DBからgithubNameを取得
         *------------------------------------------------------------*/
        FirebaseToken firebaseToken = userService.verifyJWT(JWTToken);
        if (firebaseToken == null || firebaseToken.getUid() == null) {
            Map<String, Object> response = new VerifyResponseBuilder().success(false).addError("DBにデータが存在しません。ログインし直してください").build();
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
            return deferredResult;
        }

        User user = userService.getPersonalInfomation(firebaseToken.getUid());
        if (user == null) {
            Map<String, Object> response = new VerifyResponseBuilder().success(false).addError("DBにデータが存在しません。ログインし直してください").build();
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
            return deferredResult;
        }

        /*-------------------------------------------------------------
         * githubNameを元にgitHubAPIを叩く(同期)
         *------------------------------------------------------------*/
        String response = githubService.fetchGitHubData(user.getGitName());
        JsonNode jsonNode = githubService.StringToJSON(response);

        if (response.isEmpty() || jsonNode.isEmpty()) {
            Map<String, Object> responseAPIError = new VerifyResponseBuilder().success(false).addError("データ取得に失敗しました、時間をおいてアクセスを試してください").build();
            deferredResult.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseAPIError));
            return deferredResult;
        }

        /*-------------------------------------------------------------
         * 一週間分のレポジトリ取得(非同期)
         *------------------------------------------------------------*/
        githubService.getWeeklyCommitRateAsync(user.getGitName())
                .doOnSuccess(contributions -> {                     //成功時
                    logger.debug("★ サービスからの非同期結果: " + Arrays.toString(contributions));
                    responseUserRepositoryDate.put("weeklyCommitRate", contributions);
                })
                .doOnError(error -> {
                    logger.error("★ 非同期処理でエラー発生", error);           // 失敗時
                    responseUserRepositoryDate.put("weeklyCommitRate", null);
                })
                .doFinally(signal -> {
                    // 非同期処理が終わったら Map に値をセットして ResponseEntity を返す
                    logger.debug("★★★非同期処理終わりセットdata:"+responseUserRepositoryDate);
                    deferredResult.setResult(ResponseEntity.ok(responseUserRepositoryDate));
                })
                .subscribe();   //非同期処理を実行（subscribeしないと始まらないっぽい）

        /*-------------------------------------------------------------
         * (一番最新の)コミットされたリポジトリを取得
         *------------------------------------------------------------*/
        JsonNode latestCommitRepository = githubService.getTheDayOfTheMostMomentCommit(jsonNode);
        logger.debug("latestCommitRepository:" + latestCommitRepository);

        /*-------------------------------------------------------------
         * 今日のコミットがあるかどうか
         *------------------------------------------------------------*/
        boolean isTodayCommit = githubService.getTodayCommit(latestCommitRepository);
        logger.debug("今日のコミットの結果=" + isTodayCommit);
        responseUserRepositoryDate.put("isCommitToday", isTodayCommit);

        /*-------------------------------------------------------------
         * 最新リポジトリの言語使用率を取得
         *------------------------------------------------------------*/
        Map<String, Object> userOfLatestRepositoryLanguageRatio = githubService.getLatestRepositoryLanguageRatio(latestCommitRepository);
        responseUserRepositoryDate.put("languageUsage", userOfLatestRepositoryLanguageRatio);

        //  非同期処理の完了を待ち、レスポンスを返す
        return deferredResult;
    }

}
