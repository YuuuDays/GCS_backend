package com.example.GCS.service;

import com.example.GCS.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class RegisterService {
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);

    private ResponseEntity<Map<String, Object>> createErrorResponse(String msg) {

    }

    /*
     * MEMO
     * フロントに返すJSONの例
     * // 成功の場合
        {
            "success": true,
            "data": { ... }
        }

        // エラーの場合
        {
            "success": false,
            "errors": {
                "notificationEmail": "このメールアドレスは既に登録されています",
                "gitName": "GitHubユーザー名が存在しません"
         }
     */
    public ResponseEntity<Map<String, Object>> registerCheck(User user) {

        logger.debug("user:" + user);
        //ここでバリデーションチェックserviceに飛ばして検査してもう

        Map<String, Object> response = new HashMap<>();
        return ResponseEntity.ok(response);
    }

}
