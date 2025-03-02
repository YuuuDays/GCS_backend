package com.example.GCS.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final FirebaseAuth firebaseAuth;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    /* 概要  : httpのheaderのStringを検証
     * 戻り値: FirebaseToken(Firebase Authenticationの発行されたJWTを検証した結果を保持するオブジェクト)
     */
    public FirebaseToken verifyJWT(String idToken)
    {
        String replaceIdToken = idToken.replace("Bearer ", "");

        //ユーザー情報を含むFirebaseTokenオブジェクトが返される
        try{
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(replaceIdToken);
            logger.debug("★decodedToken:" + decodedToken);
            return decodedToken;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Invalid token: " + e.getMessage(), e);

        }
    }

    /* 概要  : JWTのuidとRequestBodyのUID比較を検証
     * 戻り値: true/false
     */
    public Boolean ComparisonOfUID(FirebaseToken firebaseToken, Map<String,
                                    String> requestBody)
    {
        return true;
    }


}
