package com.example.GCS.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private FirebaseAuth firebaseAuth;
    private UserService userService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        userService = new UserService(firebaseAuth);
    }

    @Test
    void verify_normal() throws FirebaseAuthException {
        // モックの戻り値を定義
        FirebaseToken mockToken = Mockito.mock(FirebaseToken.class);    //クラスモックオブジェクト
        when(mockToken.getUid()).thenReturn("testUid");
        when(firebaseAuth.verifyIdToken(anyString())).thenReturn(mockToken);

        // テスト対象のメソッドを呼び出し
        FirebaseToken result = userService.verifyJWT("Bearer validToken");

        // アサーション
        assertNotNull(result);
        assertEquals("testUid", result.getUid());
        Mockito.verify(firebaseAuth).verifyIdToken("validToken");
    }

    @Test
    void verify_abnormal() throws FirebaseAuthException {
        FirebaseAuthException mockException = Mockito.mock(FirebaseAuthException.class);
        when(mockException.getMessage()).thenReturn("Invalid token");
        when(firebaseAuth.verifyIdToken(anyString())).thenThrow(mockException);

        // 例外がスローされることを確認
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.verifyJWT("Bearer invalidToken");
        });

        // 例外メッセージをデバッグ出力
        System.out.println("Exception message: " + exception.getMessage());

        // 例外メッセージが期待通りか確認
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    void comparisonOfUID_normal()
    {
        //Arrange
        FirebaseToken mockToken = Mockito.mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("testUID");
        Map<String,String>mockRequestBody = Map.of("uid","testUID");
        //Act
        Boolean result = userService.ComparisonOfUID(mockToken,mockRequestBody);
        //Assert
        assertEquals(true, result);
    }
}