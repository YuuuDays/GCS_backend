package com.example.GCS.controller;

import com.example.GCS.service.AuthService;
import com.example.GCS.service.RegisterService;
import com.example.GCS.utils.ResponseBuilder;
import com.example.GCS.utils.VerifyResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.http.ResponseEntity;
import com.example.GCS.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final RegisterService registerService;


    public AuthController(AuthService authService, RegisterService registerService) {
        this.authService = authService;
        this.registerService = registerService;
    }

    /*
     * Google認証データから新規登録or既存ユーザ(ログイン)か判断
     */
    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String idToken)
    {
        // JWTトークンの検証
        VerifyResponseBuilder responseBuilder =  authService.verifyToken(idToken);

        //成功判定
        if(!responseBuilder.getSuccess())
        {
            // - HTTP Status 401（Unauthorized）を設定
            logger.debug("失敗->responseBuilder.build():"+responseBuilder.build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBuilder.build());
        }

        //レスポンス組み立て
        // new ResponseEntity<>(response, HttpStatus.OK);
        logger.debug("成功->responseBuilder.build():"+responseBuilder.build());
        return ResponseEntity.ok().body(responseBuilder.build());
    }

    /**
     * 概要: JWT+ユーザー情報を取得しヴァリデーションチェック後、登録する
     * @param   JWTToken requestHeader内に格納されたJWT
     * @param   user     新規登録画面のユーザーデータ
     * @return  フロントへJSON形式で送信される
     *            {
     *             "notificationEmail": "通知を受け取るメールアドレス",
     *             "gitName": "Gitのユーザー名",
     *             "time": "希望する通知時間"
     *             }
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestHeader("Authorization") String JWTToken
                                                            ,@RequestBody User user) {
        logger.debug("JWTToken"+JWTToken);
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
         * ユーザーデータの検証
         *------------------------------------------------------------*/
        ResponseBuilder toFrontResponseBuilder = registerService.register(user,JWTToken);
        // 引数異常
        if(!toFrontResponseBuilder.getSuccess())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(toFrontResponseBuilder.build());
        }

        logger.debug("★/registerのres =" + toFrontResponseBuilder.build() );
        return ResponseEntity.ok(toFrontResponseBuilder.build());

    }
}