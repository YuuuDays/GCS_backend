package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.repository.UserRepository;
import com.example.GCS.utils.VerifyFirebaseToken;
import com.example.GCS.utils.VerifyResponseBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerifyFirebaseToken verifyFirebaseToken;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, FirebaseAuth firebaseAuth, VerifyFirebaseToken verifyFirebaseToken, VerifyResponseBuilder verifyResponseBuilder) {
        this.userRepository = userRepository;
        this.verifyFirebaseToken = verifyFirebaseToken;
    }

    /*
     * FirebaseJWTと取り出し新規登録orログイン遷移
     */
    public VerifyResponseBuilder verifyToken(String idToken) {
        // 検証後のJWT
        FirebaseToken decodedToken;
        // ↑から取り出したUID
        String uid;

        // tokenの検証
        try {
            decodedToken = verifyFirebaseToken.verifyFirebaseToken(idToken);
            uid = decodedToken.getUid();
        } catch (RuntimeException e) {
            logger.debug("★FirebaseJWT取り出しエラー:"+e.getMessage());
            return new VerifyResponseBuilder().success(false).addError("JWTトークンが不正です");
        }

        /*-------------------------------------------------------------
         * 初回ログインか既存ログインか判定
         *------------------------------------------------------------*/
        Optional<User> existingUser = userRepository.findByfirebaseUid(uid);
        logger.debug("existingUser:"+existingUser);
        if(existingUser.isEmpty())
        {
            // 初回ログイン判定
            return new VerifyResponseBuilder().success(true).needsRegistration(true);
        }
        // 既存ログイン判定
        return new VerifyResponseBuilder().success(true).needsRegistration(false);

        }

    }



