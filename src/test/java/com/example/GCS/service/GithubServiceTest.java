package com.example.GCS.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;

class GithubServiceTest {

    @Mock
    private GithubService githubService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        githubService = new GithubService();
    }

    @Test
    void parseToLocalDateTime_normal()
    {
        //テスト準備
        String setUpMoji = "2025/03/14 20:56:07.905";
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 14, 20, 56, 7);

        //テスト対象メソッドの実行
        LocalDateTime result = githubService.parseToLocalDateTime(setUpMoji);

        //検証
        assertEquals(result,dateTime);
    }
    @Test
    void parseToLocalDateTime_ArgmentError()
    {
        //テスト準備
        String setUpMoji = "";

        //テスト対象メソッドの実行
        LocalDateTime result = githubService.parseToLocalDateTime(setUpMoji);

        //検証
        assertEquals(result,LocalDateTime.now());
    }

}