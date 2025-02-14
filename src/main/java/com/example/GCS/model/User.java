package com.example.GCS.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Document
@Data
public class User {
    @Id
    private String id;
    private String googleId;
    private String email;
    private String name;
} 