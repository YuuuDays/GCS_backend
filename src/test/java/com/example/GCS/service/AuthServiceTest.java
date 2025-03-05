package com.example.GCS.service;

import com.example.GCS.repository.UserRepository;
import com.example.GCS.utils.VerifyFirebaseToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FirebaseAuth firebaseAuth;
    @Mock
    private VerifyFirebaseToken verifyFirebaseToken;

    private AuthService authService;
    public static FirebaseToken mockToken;
    public static String idToken;

//    @BeforeAll
//    static void oneSetup()
//    {
//
//
//    }

    @BeforeEach
    void setUp() {
        //ここで明示するので@Mockが使える
        MockitoAnnotations.openMocks(this);
        //LoginServiveのDIとしてMockを注入
        authService = new AuthService(userRepository,firebaseAuth,verifyFirebaseToken);
    }

    @Test
    void verifyToken_Normal() {
        // テストデータの準備
         idToken = "Bearer test-token";

        // テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = authService.verifyToken(idToken);

        // 検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void test() throws FirebaseAuthException {

        String idToken = "Bearer test-token";
        // FirebaseTokenをモック化
        mockToken = mock(FirebaseToken.class);
        //モックのふるまいを定義
        when(mockToken.getUid()).thenReturn("test-user-Id");
        when(mockToken.getEmail()).thenReturn("test@yahoooo.co.jp");
        // firebaseAuthの振る舞いを設定
        when(firebaseAuth.verifyIdToken("test-token")).thenReturn(mockToken);
        // When（テスト対象の処理を実行）
        ResponseEntity<Map<String, Object>> response = authService.verifyToken(idToken);

        // Then（結果の検証）
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(firebaseAuth).verifyIdToken("test-token"); // メソッドが呼ばれたことを確認
    }

    @Test
    void test_fault() throws FirebaseAuthException {
        // ===== テストの準備フェーズ =====
        // Bearer トークンを準備（実際のトークンの形式を模倣）
        String idToken = "Bearer test-token";
        
        // FirebaseAuthExceptionのモックオブジェクトを作成
        // これは認証失敗時のエラーをシミュレートするため
        FirebaseAuthException expectedException = mock(FirebaseAuthException.class);
        
        // モックの振る舞いを設定：getMessage()が呼ばれたら"認証エラーが発生しました"を返すように
        when(expectedException.getMessage()).thenReturn("認証エラーが発生しました");
        
        // firebaseAuthのverifyIdTokenメソッドが呼ばれたら例外を投げるように設定
        // "test-token"（Bearerプレフィックスを除いたトークン）で呼び出された時に動作
        when(firebaseAuth.verifyIdToken("test-token")).thenThrow(expectedException);

        // ===== テストの実行フェーズ =====
        // 実際にLoginServiceのverifyTokenメソッドを呼び出し
        ResponseEntity<Map<String, Object>> response = authService.verifyToken(idToken);

        // ===== 検証フェーズ =====
        // 期待される応答データの準備
        Map<String, Object> expectedErrorResponse = new HashMap<>();
        expectedErrorResponse.put("success", false);          // 認証失敗を示すフラグ
        expectedErrorResponse.put("error", "認証に失敗しました");  // エラーメッセージ
        expectedErrorResponse.put("message", "認証エラーが発生しました");  // 詳細エラーメッセージ

        // レスポンスの検証
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());  // HTTPステータスが401であることを確認
        assertEquals(expectedErrorResponse, response.getBody());         // エラーレスポンスの内容を検証
        
        // メソッドの呼び出し検証
        // firebaseAuth.verifyIdTokenが正しいパラメータで1回呼ばれたことを確認
        verify(firebaseAuth).verifyIdToken("test-token");
    }

    @Test
    void verifyToken_Abnormal() {
        // テストデータの準備
        String idToken = "";

        // テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = authService.verifyToken(idToken);

        // 検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}