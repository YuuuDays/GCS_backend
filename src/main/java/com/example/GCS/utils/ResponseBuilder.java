package com.example.GCS.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

//概要:フロントへ返すレスポンスを作成するひな形

/* ★使用例★
// エラーの場合
    new ResponseBuilder()
        .success(false)
        .addError("notificationEmail", "このメールアドレスは既に登録されています")
        .addError("gitName", "GitHubユーザー名が存在しません")
        .build();

// 成功の場合
    new ResponseBuilder()
        .success(true)
        .addData("userId", 123)
        .addData("username", "test_user")
        .build();
*/
public class ResponseBuilder {
    private final Map<String, Object> responseMap = new HashMap<>();
    private final Map<String, String> errorsMap = new HashMap<>();
    private final Map<String, Object> dataMap = new HashMap<>();
    private HttpStatus status = HttpStatus.OK;

    public ResponseBuilder success(boolean success) {
        responseMap.put("success", success);
        return this;
    }

    // エラーを追加するメソッド
    public ResponseBuilder addError(String field, String message) {
        errorsMap.put(field, message);
        status = HttpStatus.BAD_REQUEST;
        return this;
    }

    // データを追加するメソッド
    public ResponseBuilder addData(String key, Object value) {
        dataMap.put(key, value);
        return this;
    }

    public ResponseEntity<Map<String, Object>> build() {
        // エラーがある場合
        if (!errorsMap.isEmpty()) {
            responseMap.put("errors", errorsMap);
        }
        
        // データがある場合
        if (!dataMap.isEmpty()) {
            responseMap.put("data", dataMap);
        }

        return ResponseEntity.status(status).body(responseMap);
    }
} 


// メソッドチェーンなしの場合
/*
    ResponseBuilder builder = new ResponseBuilder();
    builder.success(false);
    builder.message("エラーが発生しました");
    builder.addData("timestamp", new Date());
    builder.addData("code", "E001");
    ResponseEntity<Map<String, Object>> response = builder.build(); 
*/
//直接MAP操作
/*
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("success", false);
    responseMap.put("error", "エラーが発生しました");
    responseMap.put("timestamp", new Date());
    responseMap.put("code", "E001");
    ResponseEntity<Map<String, Object>> response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
 */