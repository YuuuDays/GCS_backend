package com.example.GCS.controller;

import com.example.GCS.service.LoginService;
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
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final LoginService loginService;

    @Autowired
    private UserRepository userRepository;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

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
            logger.debug("★FirebaseToken decodedToken:"+decodedToken);
            // レスポンス用のMapを初期化
            Map<String, Object> response = new HashMap<>();
            
            // デコードされたトークンからUIDを取得し、既存ユーザーを検索
            // @return Optional<User> - ユーザーが存在する場合はユーザー情報、存在しない場合は空のOptional
            Optional<User> existingUser = userRepository.findByGoogleId(decodedToken.getUid());
            
            if (existingUser.isPresent()) {
                logger.debug("--- ★ログイン画面 ---");
                // 既存ユーザーの場合の処理
                // - ユーザー情報をレスポンスに含める
                // - isNewUserフラグをfalseに設定
                logger.info("Existing user found: {}", decodedToken.getEmail());
                response.put("success", true);
                response.put("user", existingUser.get());
                response.put("isNewUser", false);
                return ResponseEntity.ok(response);
            } else {
                logger.debug("--- ★新規登録画面 ---");
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
        /*引数の説明...フロントからJSON形式で送信される
            {
            "googleId": "Firebase認証から取得したUID",
            "notificationEmail": "通知を受け取るメールアドレス",
            "gitName": "Gitのユーザー名",
            "time": "希望する通知時間"
            }
        */


        //DBG
        logger.debug("user:"+user);
        //
        Map<String, Object> errorResponse = new HashMap<>();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        logger.info("Received user registration request for: {}", user.getNotificationEmail());
//        try {
//            // 必須フィールドの検証
//            if (user.getGoogleId() == null || user.getNotificationEmail() == null ||
//                user.getGitName() == null || user.getTime() == null) {
//                throw new IllegalArgumentException("必須フィールドが不足しています");
//            }
//
//            // 作成日時を設定
//            user.setCreatedAt(LocalDateTime.now());
//            user.setNotificationEnabled(true);  // デフォルトで通知を有効化
//
//            User savedUser = userRepository.save(user);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("user", savedUser);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("User registration failed", e);
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("error", "ユーザー登録に失敗しました");
//            errorResponse.put("message", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
    }
}