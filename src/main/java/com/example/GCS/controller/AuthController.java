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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        logger.info("Received request to /api/auth/hello endpoint");
        return ResponseEntity.ok("Hello from backend!");
    }

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