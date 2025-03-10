package com.example.GCS.service;

import com.example.GCS.dto.RegisterDTO;
import com.example.GCS.model.User;
import com.example.GCS.repository.RegisterRepository;
import com.example.GCS.utils.ResponseBuilder;
import com.example.GCS.utils.VerifyFirebaseToken;
import com.example.GCS.utils.VerifyResponseBuilder;
import com.example.GCS.validation.ValidationResult;
import com.google.firebase.auth.FirebaseToken;
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
    private final VerifyFirebaseToken verifyFirebaseToken;

    public RegisterService(ValidationChecksService validationChecksService, RegisterRepository registerRepository, VerifyFirebaseToken verifyFirebaseToken)
    {
        this.validationChecksService = validationChecksService;
        this.registerRepository = registerRepository;
        this.verifyFirebaseToken = verifyFirebaseToken;
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
    public ResponseBuilder register(User user, String JWTToken) {

        logger.debug("user:" + user);
        // UID用変数
        String UID;

        /*-------------------------------------------------------------
         * メールアドレスチェック
         *------------------------------------------------------------*/
        // メアドチェック
        ValidationResult errorJugMail = validationChecksService.checkSendMail(user.getNotificationEmail(),false);

        // 何らかのバリデーションチェックエラーの場合
        if( !errorJugMail.isValid() )
        {
            logger.debug("★errorJug_mail that error is = " + errorJugMail.getErrorMessage());
            return  new ResponseBuilder()
                    .success(false)
                    .addError(errorJugMail.getField(),errorJugMail.getErrorMessage());
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
                    .addError(errorJugGithub.getField(),errorJugGithub.getErrorMessage());
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
                    .addError(errorJugTime.getField(),errorJugTime.getErrorMessage());
        }
        /*-------------------------------------------------------------
         * JWT検証(本来はController呼び出し前検証済みの為不要)
         *------------------------------------------------------------*/
        try {
            FirebaseToken decodedToken = verifyFirebaseToken.verifyFirebaseToken(JWTToken);
            UID = decodedToken.getUid();
        } catch (RuntimeException e) {
            logger.debug("★FirebaseJWT取り出しエラー:"+e.getMessage());
            return new ResponseBuilder()
                    .success(false)
                    .addError("error","JWTトークンが不正です");
        }
        /*-------------------------------------------------------------
         * バリデーションチェック済みのモデルをDTOへ
         *------------------------------------------------------------*/
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFirebaseUid(UID);
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
                    .addError("error","DB登録失敗");
        }
        //ここに入ったら成功
        logger.debug("★DBに登録成功");
        return new ResponseBuilder()
                .success(true);
    }

}
