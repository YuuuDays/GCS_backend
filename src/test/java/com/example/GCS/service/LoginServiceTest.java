package com.example.GCS.service;

import com.example.GCS.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {

    @Mock
    private UserRepository userRepository;
    private LoginService loginService;
    private FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginService = new LoginService(userRepository,firebaseAuth);
    }

    @Test
    void verifyToken_Normal() {
        // テストデータの準備
        String idToken = "Bearer test-token";

        // テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = loginService.verifyToken(idToken);

        // 検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void test

    @Test
    void verifyToken_Abnormal() {
        // テストデータの準備
        String idToken = "";

        // テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = loginService.verifyToken(idToken);

        // 検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}