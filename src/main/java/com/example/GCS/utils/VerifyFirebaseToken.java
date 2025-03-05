package com.example.GCS.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public class VerifyFirebaseToken {
    private final FirebaseAuth firebaseAuth;

    public VerifyFirebaseToken(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    //概要:フロンエンドのAuthorizationのJWTを検証しuidを返す
    public String verifyFirebaseToken(String bearerToken)
    {
        try
        {
            // JWTを検証してuidを取得
            String idToken = bearerToken.substring(7);
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            return uid;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }
}
