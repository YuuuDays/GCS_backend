package com.example.GCS.config;
import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getGithubToken() {
        return dotenv.get("GITHUB_TOKEN");
    }
}
