package com.example.GCS.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *  概要:フロントへ返すレスポンスを作成するひな形
 *  成功時
 *  return new ResponseBuilder()
 *     .success(true)
 *     .addData("token", "JWTToken")
 *     .addData("notificationEmail", "xxx@xxx.com")
 *      ...(省略)
 *     .build();
 *  失敗時
 *  return new ResponseBuilder()
 *     .success(false)
 *     .addError("token", "JWTトークンが不正です")
 *     .build();
 */
@Component
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

    // 各種Getterメソッド
    public Boolean getSuccess() {
        return (Boolean) responseMap.get("success");
    }

    public Map<String, String> getErrors() {
        return errorsMap;
    }

    public Object getData(String key) {
        return dataMap.get(key);
    }

    public HttpStatus getStatus() {
        return status;
    }

    // Mapを取得
    public Map<String, Object> build() {
        if (!errorsMap.isEmpty()) {
            responseMap.put("errors", errorsMap);
        }
        if (!dataMap.isEmpty()) {
            responseMap.put("data", dataMap);
        }
        return responseMap;
    }
}