package com.example.GCS.service;

import com.example.GCS.validation.ValidationResult;
import com.example.GCS.repository.UserRepository;
import com.example.GCS.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import java.util.Optional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * 概要:新規登録(やログイン時)のフロントからの引数が規定通りか調べる
 * (作成理由:のちチェック事項が増えそうなので別に作成)
 */
@Service
public class ValidationChecksService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationChecksService.class);
    private final UserRepository userRepository;

    public ValidationChecksService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 概要:通知用メールアドレスのチェック
    public ValidationResult checkSendMail(String mail)
    {
        // null or 空文字チェック
        if(mail == null || mail.isEmpty())
        {
            return  ValidationResult.error("email","メールアドレスが空です");
        }
        
        //　形式チェック
        try{
            InternetAddress internetAddress = new InternetAddress(mail);
            internetAddress.validate();
        }catch(AddressException e){
            return  ValidationResult.error("email","メールアドレスが不正です");
        }

        // 通知用メアド使用済みじゃないかどうか
        Optional<User> userOpt = userRepository.findByNotificationEmail(mail);
        if (userOpt.isPresent()) {
            return ValidationResult.error("email", userOpt.get().getNotificationEmail() + "は既に使用されています");
        }

        return ValidationResult.success();
    }


    // 概要: 与えられた引数の文字列を使いgithubアカウントが存在するかどうか
    public ValidationResult checkGitHubAccount(String userName) {

        //null or 空文字チェック
        if(userName == null || userName.isEmpty())
        {
            return  ValidationResult.error("github","GitHubアカウント文字列が空です");
        }
        String urlString = "https://api.github.com/users/" + userName;

        try{
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            int resCode = connection.getResponseCode();
            connection.disconnect();

            // 404の場合、アカウントが存在しないと判断
            if (resCode == 404) {
                logger.debug("★githubAccount is none");
                return ValidationResult.error("github", "GitHubアカウントが存在しません");
            }
            logger.debug("★githubAccount　正常確認");
            return ValidationResult.success();

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("checkGitHubAccount error:"+ e);
            return ValidationResult.error("github", "GitHubアカウントの検証中にエラーが発生しました");
        }
    }
}
