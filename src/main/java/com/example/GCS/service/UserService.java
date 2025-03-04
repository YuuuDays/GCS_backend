package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.repository.UserRepository;
import com.example.GCS.validation.ValidationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(FirebaseAuth firebaseAuth, UserRepository userRepository) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
    }

    /* 概要  : httpのheaderのStringを検証
     * 戻り値: FirebaseToken(Firebase Authenticationの発行されたJWTを検証した結果を保持するオブジェクト)
     */
    public FirebaseToken verifyJWT(String idToken)
    {
        if(idToken == null || idToken.isEmpty())
        {
            throw new IllegalArgumentException("ID Token is null or empty");
        }

        String replaceIdToken = idToken.replace("Bearer ", "");

        //ユーザー情報を含むFirebaseTokenオブジェクトが返される
        try{
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(replaceIdToken);
            logger.debug("★decodedToken:" + decodedToken);
            return decodedToken;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Invalid token: " + e.getMessage(), e);

        }
    }

    /* 概要  : JWTのuidとRequestBodyのUID比較を検証
     * 戻り値: true  ...第1,2引数一致
     *        false ...第1,2引数不一致
     */
    public Boolean ComparisonOfUID(FirebaseToken firebaseToken, Map<String,
                                    String> requestBody)
    {
        // ここにくるfirebaseTokenは検証済みのはずなので無視
        if(requestBody == null || requestBody.isEmpty())
        {
            throw new IllegalArgumentException("requestBody is null or empty");
        }
        // キー"uid"が存在しない、または値が空文字の場合
        if (!requestBody.containsKey("uid") || requestBody.get("uid").isEmpty()) {
            throw new IllegalArgumentException("uid is empty or missing");
        }

        //比較
        if(firebaseToken.getUid().equals(requestBody.get("uid")))
        {
            return true;
        }
        return false;

    }

    /* 概要  : uidを元にDBの値を取得する
     * 戻り値: User ... uidに紐づく個人情報を取得する
     */
    public User getPersonalInfomation(String uid)
    {

        Optional<User> optionalUser = userRepository.findByGoogleId(uid);

        if(optionalUser.isEmpty())
        {
            // Controller側でnull判定
            return null;
        }

        // 返答用
        User verifiedUser = optionalUser.get();
        return verifiedUser;
    }

    // 概要: 修正されたユーザー情報をDBに登録する
    public ValidationResult amendmentRegisteration(Map<String, String> requestBody)
    {

        try{
            Optional<User> optionalUser = userRepository.findByGoogleId(requestBody.get("uid"));
            if (optionalUser.isPresent())
            {
                User user = optionalUser.get();
                logger.debug("★登録前DB値:"+user);

                // 更新対象のフィールドをセット
                user.setNotificationEmail(requestBody.get("notificationEmail"));
                user.setGitName(requestBody.get("gitName"));
                user.setTime(requestBody.get("notificationTime"));
                // DBの値を更新
                userRepository.save(user);
            }else
            {
                return ValidationResult.error("error","DB登録失敗");
            }
        }catch (DataAccessException | MongoException e)
        {
            logger.debug("error :"+ e.getMessage());
            return ValidationResult.error("error","DB登録失敗");
        }
        return ValidationResult.success();
    }

    //概要: ユーザー情報削除
    public ValidationResult deleteUserInfo(User user)
    {

        if (userRepository.existsById(user.getId())) {
            userRepository.deleteById(user.getId());
            return ValidationResult.success();
        }
        return ValidationResult.error("error","User not found");
    }
}
