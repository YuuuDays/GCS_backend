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
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            // Firebaseトークンの検証のみで認証完了
            FirebaseToken decodedToken = FirebaseAuth.getInstance()
                .verifyIdToken(idToken.replace("Bearer ", ""));
            
            // ユーザー情報の保存や取得
            User user = userRepository.findByGoogleId(decodedToken.getUid())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGoogleId(decodedToken.getUid());
                    newUser.setEmail(decodedToken.getEmail());
                    newUser.setName(decodedToken.getName());
                    return userRepository.save(newUser);
                });

            return ResponseEntity.ok(user);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}