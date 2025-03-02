package com.example.GCS.controller;


import com.example.GCS.service.UserService;
import com.example.GCS.utils.ResponseBuilder;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 概要:user操作に関わるコントローラー
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 概要: ログイン後のユーザ情報表示
    @PostMapping("/info")
    public ResponseEntity<Map<String,Object>> editUserInfo(@RequestHeader("Authorization") String idToken,
                                                           @RequestBody Map<String, String> requestBody)
    {
        logger.debug("★idToken is " + idToken);
        logger.debug("★requestBody is " + requestBody);

        // JWT検証用
        FirebaseToken firebaseToken;

        /* ------------------------------
         * JWT検証
         -------------------------------- */
        try
        {
            firebaseToken = userService.verifyJWT(idToken);
        } catch (RuntimeException e) {
            logger.error("Token verification failed: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        /* ------------------------------
         * JWTのuidとRequestBodyのuid比較
         -------------------------------- */


        ResponseEntity<Map<String, Object>> response = new ResponseBuilder().success(false).build();
        return response;
    }
}
