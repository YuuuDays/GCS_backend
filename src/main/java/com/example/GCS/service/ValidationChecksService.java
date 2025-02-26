package com.example.GCS.service;

import com.example.GCS.model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * 概要:新規登録(やログイン時)のフロントからの引数が規定通りか調べる
 * (作成理由:のちチェック事項が増えそうなので別に作成)
 */
public class ValidationChecksService {


    public boolean checkNULLThatUserOfModel(User user)
    {
        return true;
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
