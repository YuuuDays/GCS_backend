package com.example.GCS.controller;

import com.example.GCS.service.AuthService;
import com.example.GCS.service.RegisterService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.http.ResponseEntity;
import com.example.GCS.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String idToken) {

        return authService.verifyToken(idToken);
    }


    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        /*引数の説明...フロントからJSON形式で送信される
            {
            "googleId": "Firebase認証から取得したUID",
            "notificationEmail": "通知を受け取るメールアドレス",
            "gitName": "Gitのユーザー名",
            "time": "希望する通知時間"
            }
        */
        ResponseEntity<Map<String,Object>> res = registerService.register(user);
        logger.debug("★/registerのres =" + res );
        return res;

    }
}