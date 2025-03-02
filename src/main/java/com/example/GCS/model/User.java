package com.example.GCS.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;

import java.time.LocalDateTime;

@Document(collection = "user_info") // MongoDBのコレクション名
@Data
public class User {

    /*必須 */
    @Indexed(unique = true)     // インデックスを追加して検索を高速化
    private String googleId;     // Googleログイン用のUID

    @Indexed(unique = true)     // メールアドレスも一意にする
    @Field("mail")// 通知用のメールアドレス（必須）
    private String notificationEmail;

    @Field("git_name")// Git名()
    private String gitName;

    @Field("time")// 通知用時間
    private String time;

    @Field("notification_enabled")
    private boolean notificationEnabled; // メール通知の有効/無効

    /* 任意 */
    @Id
    private String id;          // MongoDBの内部ID

    private String username;        // ユーザー名

    private String photoUrl;     // プロフィール画像URL（Googleから取得可能）


}