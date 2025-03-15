package com.example.GCS.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// 概要:githubのデータを取得するクラス
public class GithubService {

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    public LocalDateTime parseToLocalDateTime(String clientTimestamp)
    {
        try {
            String formattedTimestamp = clientTimestamp.split("\\.")[0];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            return LocalDateTime.parse(formattedTimestamp, formatter);
        } catch (DateTimeParseException e) {
            logger.warn("引数異常->\"" + clientTimestamp + "\"デフォルト値を使います");
            return LocalDateTime.now();  // デフォルトで現在時刻を返す
        }

    }

//    public String getGitHubName(St)
//    {
//       return "";
//    }
}
