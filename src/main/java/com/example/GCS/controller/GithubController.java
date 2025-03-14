package com.example.GCS.controller;


import com.example.GCS.service.AuthService;
import com.example.GCS.utils.VerifyResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//概要:/dashBoardへ返すAPIデータ
@RestController
@RequestMapping("/github")
public class GithubController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(GithubController.class);

    public GithubController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 概要: JWT+ユーザー情報を取得しヴァリデーションチェック後、登録する
     * @param   JWTToken requestHeader内に格納されたJWT
     * @return  フロントへJSON形式で送信される
     *            {
     *             "languageUsage": "(ユーザの公開レポジトリにあるすべての)言語使用率",
     *             "isCommitToday": "今日のコミットの有無",
     *             "weeklyCommitRate": "1週間のコミット率",
     *             "monthlyCommitRate": "一ヶ月のコミット率",
     *             "lastCachedDateTime": "(最後にキャッシュされた)日付+時間"
     *             }
     */
    @GetMapping("/userDate")
    public ResponseEntity<Map<String, Object>> getGitHubData(@RequestHeader("Authorization") String JWTToken) {
        /*-------------------------------------------------------------
         * JWTトークンの検証
         *------------------------------------------------------------*/
        VerifyResponseBuilder JWTResponseBuilder =  authService.verifyToken(JWTToken);

        //成功判定
        if(!JWTResponseBuilder.getSuccess())
        {
            // - HTTP Status 401（Unauthorized）を設定
            logger.debug("失敗->JWTResponseBuilder.build():"+JWTResponseBuilder.build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(JWTResponseBuilder.build());
        }
        // DBからgithubNameを取得
        // githubNameを元にgithubapiを叩くorキャッシュからデータを取得
        // レスポンスデータを返す

        return ResponseEntity.ok().build();
    }
}
