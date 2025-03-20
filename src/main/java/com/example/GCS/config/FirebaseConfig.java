package com.example.GCS.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;


import com.google.firebase.auth.FirebaseAuth;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final String SERVICE_ACCOUNT_PATH = "serviceAccountKey.json"; // 直接設定

    @PostConstruct
    public void initialize() {
        try {
            System.out.println("Loading Firebase config from: " + SERVICE_ACCOUNT_PATH);

            InputStream serviceAccount = new ClassPathResource(SERVICE_ACCOUNT_PATH).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully!");
            }
        } catch (IOException e) {
            logger.error("★FirebaseConfig Error:{}", e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }


    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
} 