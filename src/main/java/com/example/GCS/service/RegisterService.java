package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.utils.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterService {
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);
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

        // NULLチェック
        if(user == null) {
            return new ResponseBuilder()
                        .success(false)
                        .addError("error","値が無い、または不正です")
                        .build();
        }
        // メアドチェック
//        if(!ここで別のサービス関数へ(user.getLoginEmail()))
//        {
//            ここではエラーレスポンス返し
//        }

        // 初期化
        Map<String, Object> response = new HashMap<>();

        return ResponseEntity.ok(response);
    }

}
