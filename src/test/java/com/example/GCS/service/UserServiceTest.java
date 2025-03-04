package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.repository.UserRepository;
import com.example.GCS.validation.ValidationResult;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private FirebaseAuth firebaseAuth;
    @Mock
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        userService = new UserService(firebaseAuth,userRepository);
    }

    @Test
    void verifyNormal() throws FirebaseAuthException {
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
    void verifyAbnormal() throws FirebaseAuthException {
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
    void comparisonOfUIDNormal()
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
    @Test
    void comparisonOfUIDAbNormal()
    {
        //Arrange
        FirebaseToken mockToken = Mockito.mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("testUID");
        Map<String,String>mockRequestBody = Map.of("uid","djdoidasd");  //uid異常
        //Act
        Boolean result = userService.ComparisonOfUID(mockToken,mockRequestBody);
        //Assert
        assertEquals(false, result);
    }

    @Test
    void comparisonOfUIDArgumentErrorNotUID()
    {
        //Arrange
        FirebaseToken mockToken = Mockito.mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("testUID");
        Map<String,String>mockRequestBody = Map.of("uid","");   //uid(key)の値(value)無し
        //Act&Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()->
                userService.ComparisonOfUID(mockToken,mockRequestBody));

        assertEquals("uid is empty or missing",exception.getMessage());
    }
    @Test
    void comparisonOfUIDArgumentErrorNull()
    {
        //Arrange
        FirebaseToken mockToken = Mockito.mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("testUID");
        Map<String,String>mockRequestBody = Map.of();   //null
        //Act&Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()->
                userService.ComparisonOfUID(mockToken,mockRequestBody));

        assertEquals("requestBody is null or empty",exception.getMessage());
    }

    @Test
    void getPersonalInfomationNormal()
    {
        //Arrange
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        User mockUser = new User();
        mockUser.setGoogleId("1234");
        when(mockUserRepository.findByGoogleId("uid")).thenReturn(Optional.of(mockUser));
        String uid = "1234";
        //Act
        User result = userService.getPersonalInfomation(uid);
        //Assert
        assertEquals(mockUser.getGoogleId(),result.getGoogleId());
    }

    //削除処理
    @Test
    void deleteUserInfoNormal()
    {
        // Arrange
        FirebaseAuth mockFirebaseAuth = Mockito.mock(FirebaseAuth.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserService(mockFirebaseAuth,mockUserRepository);
        User mockUser = new User();
        mockUser.setId(1234L);
        when(mockUserRepository.existsById(mockUser.getId())).thenReturn(true);

        // Act
        ValidationResult result = userService.deleteUserInfo(mockUser);
        // Assert
//        assertEquals(ValidationResult.success(), result); 同じオブジェクトじゃないとだめ
        assertTrue(result.equals(ValidationResult.success()));
    }

    //削除処理
    @Test
    void deleteUserInfoArgumentError()
    {
        // Arrange
        FirebaseAuth mockFirebaseAuth = Mockito.mock(FirebaseAuth.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserService(mockFirebaseAuth,mockUserRepository);
        User mockUser = new User();
        when(mockUserRepository.existsById(mockUser.getId())).thenReturn(false);
        ValidationResult prediction = ValidationResult.error("error","User not found");

        // Act
        ValidationResult result = userService.deleteUserInfo(mockUser);

        // Assert
        assertTrue(result.isValid()==(prediction.isValid()));
    }

}