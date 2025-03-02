package com.example.GCS.service;

import com.example.GCS.dto.RegisterDTO;
import com.example.GCS.model.User;
import com.example.GCS.repository.RegisterRepository;
import com.example.GCS.utils.ResponseBuilder;
import com.example.GCS.validation.ValidationResult;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

// import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterService {
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);

    private final ValidationChecksService validationChecksService;
    private final RegisterRepository registerRepository;

    public RegisterService(ValidationChecksService validationChecksService, RegisterRepository registerRepository)
    {
        this.validationChecksService = validationChecksService;
        this.registerRepository = registerRepository;
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
    public ResponseEntity<Map<String, Object>> register(User user) {

        logger.debug("user:" + user);
        // googleIdチェック
        if(user.getGoogleId() == null || user.getGoogleId().isEmpty())
        {
            return new ResponseBuilder()
                    .success(false)
                    .addError("googleId","googleIdが不正です。もう一度最初からやり直してください")
                    .build();
        }

        /*-------------------------------------------------------------
         * メールアドレスチェック
         *------------------------------------------------------------*/
        // メアドチェック
        ValidationResult errorJugMail = validationChecksService.checkSendMail(user.getNotificationEmail());

        // 何らかのバリデーションチェックエラーの場合
        if( !errorJugMail.isValid() )
        {
            logger.debug("★errorJug_mail that error is = " + errorJugMail.getErrorMessage());
            return new ResponseBuilder()
                    .success(false)
                    .addError(errorJugMail.getField(),errorJugMail.getErrorMessage())
                    .build();
        }
        /*-------------------------------------------------------------
         * GithubAccountチェック
         *------------------------------------------------------------*/
        ValidationResult errorJugGithub =  validationChecksService.checkGitHubAccount(user.getGitName());
        // バリデーションチェック
        if( !errorJugGithub.isValid() )
        {
            logger.debug("★errorJugGithub that error is = " + errorJugGithub.getErrorMessage());
            return new ResponseBuilder()
                    .success(false)
                    .addError(errorJugGithub.getField(),errorJugGithub.getErrorMessage())
                    .build();
        }
        /*-------------------------------------------------------------
         * 通知時間チェック
         *------------------------------------------------------------*/
        ValidationResult errorJugTime = validationChecksService.checkNotificationTime(user.getTime());
        // バリデーションチェック
        if( !errorJugTime.isValid() )
        {
            logger.debug("★errorJugTime that error is = " + errorJugTime.getErrorMessage());
            return new ResponseBuilder()
                    .success(false)
                    .addError(errorJugTime.getField(),errorJugTime.getErrorMessage())
                    .build();
        }
        /*-------------------------------------------------------------
         * バリデーションチェック済みのモデルをDTOへ
         *------------------------------------------------------------*/
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setGoogleId(user.getGoogleId());
        registerDTO.setNotificationEmail(user.getNotificationEmail());
        registerDTO.setGitName(user.getGitName());
        registerDTO.setTime(user.getTime());

        /*-------------------------------------------------------------
         * DBへ登録
         *------------------------------------------------------------*/
        // レスポンス初期化
        // Map<String, Object> response = new HashMap<>();
        try
        {
            // 登録
            registerRepository.save(registerDTO);
        }catch (DataAccessException | MongoException e)
        {
            e.printStackTrace();
            logger.debug("★DB登録失敗" );
            return new ResponseBuilder()
                    .success(false)
                    .addError("error","DB登録失敗")
                    .build();
        }
        //ここに入ったら成功
        logger.debug("★DBに登録成功");
        return new ResponseBuilder()
                .success(true)
                .build();
    }

}
