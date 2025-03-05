package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.repository.UserRepository;
import com.example.GCS.utils.VerifyFirebaseToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;
    private final VerifyFirebaseToken verifyFirebaseToken;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, FirebaseAuth firebaseAuth, VerifyFirebaseToken verifyFirebaseToken) {
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
        this.verifyFirebaseToken = verifyFirebaseToken;
    }


    /*
     * FirebaseJWTと取り出し新規登録orログイン遷移
     */
    public ResponseEntity<Map<String, Object>> verifyToken(String idToken) {
        String replaceIdToken = idToken.replace("Bearer ", "");
        logger.debug("★idToken:" + idToken);
        try {
            //ユーザー情報を含むFirebaseTokenオブジェクトが返される
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(replaceIdToken);

            // @return Optional<User> - ユーザーが存在する場合はユーザー情報、存在しない場合は空のOptional
            Optional<User> existingUser = userRepository.findByGoogleId(decodedToken.getUid());

            // レスポンス用のMapを初期化
            Map<String, Object> response = new HashMap<>();

            //ここから "新規登録/ログイン" で分岐
            if (existingUser.isPresent()) {
                logger.debug("--- ★ログイン画面★ ---");
                response.put("success", true);
                response.put("user", existingUser.get());
                response.put("isNewUser", false);
            } else {
                logger.debug("--- ★新規登録★ ---");
                response.put("success", true);
                response.put("user", Map.of(
                        "googleId", decodedToken.getUid(),
                        "email", decodedToken.getEmail(),
                        "name", decodedToken.getName()
                ));
                response.put("isNewUser", true);
            }
            return ResponseEntity.ok(response);

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


}
