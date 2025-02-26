package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * 概要:新規登録(やログイン時)のフロントからの引数が規定通りか調べる
 * (作成理由:のちチェック事項が増えそうなので別に作成)
 */
@Service
public class ValidationChecksService {


    // 概要:通知用メールアドレスのチェック
    public ValidationResult checkSendMail(String mail)
    {
        if(mail == null || mail.isEmpty())
        {
            return  ValidationResult.error("email","メールアドレスが空です");
        }
        return  ValidationResult.success();
    }

    /* 与えられた引数の文字列を使いgithubアカウントが存在するか調べる有れば(200 = true)
        なければ(またはエラー= 400)false
    */
    public boolean checkGitHubAccount(String userName) {
        String urlString = "https://api.github.com/users/" + userName;

        try{
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            int resCode = connection.getResponseCode();
            connection.disconnect();

            return resCode == 200;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
