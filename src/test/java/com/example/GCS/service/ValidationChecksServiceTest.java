package com.example.GCS.service;

import com.example.GCS.repository.UserRepository;
import com.example.GCS.validation.ValidationResult;
import com.example.GCS.model.User;

import io.grpc.xds.shaded.io.envoyproxy.envoy.config.accesslog.v3.ComparisonFilter.Op;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ValidationChecksServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private URL url;

    private ValidationChecksService validationChecksService;
    private ValidationResult result;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        validationChecksService = new ValidationChecksService(userRepository);
    }

    @Test
    void shouldAcceptValidEmail() {
        // Given
        String validEmail = "test@example.com";
        // When
        ValidationResult result = validationChecksService.checkSendMail(validEmail);
        // Then
        assertThat(result, equalTo(ValidationResult.success()));
    }

    @Test
    void shouldRejectEmptyEmail() {
        // Given
        String emptyEmail = "";
        // When
        ValidationResult result = validationChecksService.checkSendMail(emptyEmail);
        // Then
        ValidationResult expected = ValidationResult.error("email", "メールアドレスが空です");
        assertThat(result, equalTo(expected));
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        // Given
        String invalidEmail = "xxx@@@yahoo.co.jp";
        // When
        ValidationResult result = validationChecksService.checkSendMail(invalidEmail);
        // Then
        ValidationResult expected = ValidationResult.error("email", "メールアドレスが不正です");
        assertThat(result, equalTo(expected));
    }

    //重複在り〼
    @Test
    void tyouhukumail() {
        //準備
        String x = "x@yahoo.co.jp";
        User user = new User();
        user.setNotificationEmail(x);
        when(userRepository.findByNotificationEmail(x)).thenReturn(Optional.of(user));
        // 実測値
        result = validationChecksService.checkSendMail(x);
        ValidationResult expected = ValidationResult.error("email", user.getNotificationEmail() + "は既に使用されています");
        assertThat(result, equalTo(expected));
    }

    //重複無し_登録
    @Test
    void accesstest() {
        String x = "sample@yahoo.co.jp";
        when(userRepository.findByNotificationEmail(x)).thenReturn(Optional.empty());

        // 実測値
        result = validationChecksService.checkSendMail(x);
        ValidationResult expected = ValidationResult.success();
        assertThat(result, equalTo(expected));
    }

    /* GitHubAccount*/
//　mock作れませんでした
    // @Test
    // void checkAccount_normal() throws Exception {
    //     // モックオブジェクトの作成
    //     HttpURLConnection connection = mock(HttpURLConnection.class);
    //     URL url = mock(URL.class);

    //     // モックの振る舞いを設定
    //     when(connection.getResponseCode()).thenReturn(200);
    //     when(url.openConnection()).thenReturn(connection);

    //     try (MockedStatic<URL> mockedUrl = mockStatic(URL.class)) {
    //         // 正しいモック化の方法
    //         mockedUrl.when(() -> new URL("https://api.github.com/users/existingUser")).thenReturn(url);

    //         ValidationResult result = validationChecksService.checkGitHubAccount("existingUser");
    //         assertThat(result.isValid(), equalTo(true));
    //     }
    // }
}