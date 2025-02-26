package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.utils.ResponseBuilder;
import com.example.GCS.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterService {
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);
    private final ValidationChecksService validationChecksService;

    public RegisterService(ValidationChecksService validationChecksService)
    {
        this.validationChecksService = validationChecksService;
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

        // NULLチェック
        if(user == null) {
            return new ResponseBuilder()
                    .success(false)
                    .addError("error","値が無い、または不正です")
                    .build();
        }
        /*-------------------------------------------------------------
         * メールアドレスチェック
         *------------------------------------------------------------*/
        // メアドチェック
        ValidationResult errorJug_mail = validationChecksService.checkSendMail(user.getNotificationEmail());

        // 何らかのバリデーションチェックエラーの場合
        if( !errorJug_mail.isValid() )
        {
            logger.debug("★errorJug_mail that error is = " + errorJug_mail.getErrorMessage());
            return new ResponseBuilder()
                    .success(false)
                    .addError(errorJug_mail.getField(),errorJug_mail.getErrorMessage())
                    .build();
        }
        /*-------------------------------------------------------------
         * GithubAccountチェック  ここから
         *------------------------------------------------------------*/
        ValidationResult errorJug_github =  validationChecksService.checkGitHubAccount(user.getGitName());
        // バリデーションチェック
        if(! errorJug_github.isValid())
        {
            logger.debug("★errorJug_github　that error is = " + errorJug_github.getErrorMessage());
            return new ResponseBuilder()
                    .success(false)
                    .addError(errorJug_github.getField(),errorJug_github.getErrorMessage())
                    .build();
        }
        /*-------------------------------------------------------------
         * バリデーションチェック終わり　登録
         *------------------------------------------------------------*/

        // 初期化
        Map<String, Object> response = new HashMap<>();

        return ResponseEntity.ok(response);
    }

}
