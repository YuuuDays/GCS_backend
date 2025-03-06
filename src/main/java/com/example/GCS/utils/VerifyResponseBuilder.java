package com.example.GCS.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
/*
// 成功パターン
    return new VerifyResponseBuilder()
        .success(true)
        .needsRegistration(false)
        .addData("token", replaceIdToken)
        .build();

// エラーパターン
    return new VerifyResponseBuilder()
        .success(false)
        .addError("JWTトークンが不正です")
        .build();
 */
@Component
public class VerifyResponseBuilder {
    private Map<String, Object> response = new HashMap<>();

    // デフォルトコンストラクタ
    public VerifyResponseBuilder() {
        response = new HashMap<>();
    }

    // 成功/失敗の設定
    public VerifyResponseBuilder success(boolean success) {
        response.put("success", success);
        return this;
    }

    // 登録が必要かどうかの設定
    public VerifyResponseBuilder needsRegistration(boolean needsRegistration) {
        response.put("needsRegistration", needsRegistration);
        return this;
    }

    // エラーメッセージの追加
    public VerifyResponseBuilder addError(String message) {
        response.put("error", message);
        return this;
    }

    // データの追加
    public VerifyResponseBuilder addData(String key, Object value) {
        response.put(key, value);
        return this;
    }

    // 各種getter
    public Boolean getSuccess() {
        return (Boolean) response.get("success");
    }

    public Boolean getNeedsRegistration() {
        return (Boolean) response.get("needsRegistration");
    }

    public String getError() {
        return (String) response.get("error");
    }

    public Object getData(String key) {
        return response.get(key);
    }

    // Mapを取得
    public Map<String, Object> build() {
        return response;
    }
}