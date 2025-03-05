package com.example.GCS.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

//概要:フロントへ返すレスポンスを作成するひな形(改良版)
public class VerifyResponseBuilder {
    /*
     * 使用例:
     *
     * // パターン1: 登録が未完了の場合
     * return new VerifyResponseBuilder()
     *         .success(true)
     *         .needsRegistration(true)
     *         .addData("user", userData)
     *         .build();
     *
     * // パターン2: 登録済みの場合
     * return new VerifyResponseBuilder()
     *         .success(true)
     *         .needsRegistration(false)
     *         .addData("user", userData)
     *         .build();
     *
     * // パターン3: エラー時
     * return new VerifyResponseBuilder()
     *         .addError("エラーメッセージ")
     *         .build();
     */
    private final Map<String, Object> response = new HashMap<>();
    private final Map<String, Object> data = new HashMap<>();
    private HttpStatus status = HttpStatus.OK;

    // 成功フラグの設定
    public VerifyResponseBuilder success(boolean isSuccess) {
        response.put("success", isSuccess);
        return this;
    }

    // 登録が必要かどうかの設定
    public VerifyResponseBuilder needsRegistration(boolean needsRegistration) {
        response.put("needsRegistration", needsRegistration);
        return this;
    }

    // エラーメッセージの追加
    public VerifyResponseBuilder addError(String message) {
        response.put("success", false);
        response.put("error", message);
        status = HttpStatus.BAD_REQUEST;
        return this;
    }

    // データの追加
    public VerifyResponseBuilder addData(String key, Object value) {
        data.put(key, value);
        return this;
    }

    // 最終レスポンスの組み立て
    public ResponseEntity<Map<String, Object>> build() {
        if (!data.isEmpty()) {
            response.put("data", data);
        }
        return ResponseEntity.status(status).body(response);
    }
}
