package com.example.GCS.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "user_info") // MongoDBのコレクション名
@Data
public class RegisterDTO {

    @Id
    private String id;          // MongoDBの内部ID
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
}
