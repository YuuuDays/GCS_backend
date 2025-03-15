package com.example.GCS.controller;


import com.example.GCS.model.User;
import com.example.GCS.service.AuthService;
import com.example.GCS.service.CacheService;
import com.example.GCS.service.GithubService;
import com.example.GCS.service.UserService;
import com.example.GCS.utils.ResponseBuilder;
import com.example.GCS.utils.VerifyResponseBuilder;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

//概要:/dashBoardへ返すAPIデータ
@RestController
@RequestMapping("/github")
public class GithubController {

    private final AuthService authService;
    private final GithubService githubService;
    private final UserService userService;
    private final CacheService cacheService;

    private static final Logger logger = LoggerFactory.getLogger(GithubController.class);

    public GithubController(AuthService authService, GithubService githubService, UserService userService, CacheService cacheService) {
        this.authService = authService;
        this.githubService = githubService;
        this.userService = userService;
        this.cacheService = cacheService;
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
    public ResponseEntity<Map<String, Object>> getGitHubData(@RequestHeader("Authorization") String JWTToken,
                                                             @RequestParam String clientTimeStamp) {
        // response用
        Map<String,Object> responseBody;
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
        /*-------------------------------------------------------------
         *  DBからgithubNameを取得
         *------------------------------------------------------------*/
        // JWTからFirebaseTokenを取得
        FirebaseToken firebaseToken = userService.verifyJWT(JWTToken);
        if(firebaseToken == null || firebaseToken.getUid() == null)
        {
            Map<String,Object> response = new VerifyResponseBuilder().success(false).addError("DBにデータが存在しません。ログインし直してください").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // FirebaseToken→uidからDBのuserデータを取得
        User user = userService.getPersonalInfomation(firebaseToken.getUid());
        if(user == null)
        {
            Map<String,Object> response = new VerifyResponseBuilder().success(false).addError("DBにデータが存在しません。ログインし直してください").build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        /*-------------------------------------------------------------
         *   githubNameを元にgitHubAPIを叩くorキャッシュからデータを取得
         *------------------------------------------------------------*/
        // キャッシュデータを取得
        Map<String, Object> cachedData = cacheService.getCache(user.getGitName());
        LocalDateTime clientTime  = githubService.parseToLocalDateTime(clientTimeStamp);

        // redisキャッシュがある場合
        if (cachedData != null)
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime cacheTime = LocalDateTime.parse(cachedData.get("timestamp").toString(), formatter);

            if (cacheTime.plusMinutes(5).isAfter(clientTime)) {
                return ResponseEntity.ok().body(cachedData);
            }
        }

        // 5分以上経過 or キャッシュなしならGitHub APIを取得
        Map<String, Object> newData = githubService.fetchGitHubData(user.getGitName());
        newData.put("timestamp", clientTime); // キャッシュの時間を保存
        cacheService.setCache(user.getGitName(), newData);
        return newData;
        // レスポンスデータを返す

        return ResponseEntity.ok().build();
    }
}
