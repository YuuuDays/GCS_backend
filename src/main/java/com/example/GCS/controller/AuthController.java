package com.example.GCS.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.example.GCS.repository.UserRepository;
import com.example.GCS.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        logger.info("Received request to /api/auth/hello endpoint");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Hello from backend!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            // リクエストの受信をログに記録
            logger.info("Received token verification request");
            
            // @param idToken: "Bearer "で始まるFirebase認証トークン
            // トークンから"Bearer "プレフィックスを削除し、FirebaseAuthで検証
            // 検証に成功すると、ユーザー情報を含むFirebaseTokenオブジェクトが返される
            FirebaseToken decodedToken = FirebaseAuth.getInstance()
                .verifyIdToken(idToken.replace("Bearer ", ""));
            
            // レスポンス用のMapを初期化
            Map<String, Object> response = new HashMap<>();
            
            // デコードされたトークンからUIDを取得し、既存ユーザーを検索
            // @return Optional<User> - ユーザーが存在する場合はユーザー情報、存在しない場合は空のOptional
            Optional<User> existingUser = userRepository.findByGoogleId(decodedToken.getUid());
            
            if (existingUser.isPresent()) {
                // 既存ユーザーの場合の処理
                // - ユーザー情報をレスポンスに含める
                // - isNewUserフラグをfalseに設定
                logger.info("Existing user found: {}", decodedToken.getEmail());
                response.put("success", true);
                response.put("user", existingUser.get());
                response.put("isNewUser", false);
                return ResponseEntity.ok(response);
            } else {
                // 新規ユーザーの場合の処理
                // - Firebaseから取得した基本情報（UID、メール、名前）をレスポンスに含める
                // - isNewUserフラグをtrueに設定
                // - この情報を元にフロントエンド側で登録処理を行うことを想定
                logger.info("New user detected: {}", decodedToken.getEmail());
                response.put("success", true);
                response.put("user", Map.of(
                    "googleId", decodedToken.getUid(),
                    "email", decodedToken.getEmail(),
                    "name", decodedToken.getName()
                ));
                response.put("isNewUser", true);
                return ResponseEntity.ok(response);
            }

        } catch (FirebaseAuthException e) {
            // トークン検証に失敗した場合のエラーハンドリング
            // - エラー内容をログに記録
            // - クライアントにエラーメッセージを返す
            // - HTTP Status 401（Unauthorized）を設定
            logger.error("Token verification failed", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "認証に失敗しました");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        logger.info("Received user registration request for: {}", user.getEmail());
        try {
            User savedUser = userRepository.save(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", savedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("User registration failed", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "ユーザー登録に失敗しました");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}