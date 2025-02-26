package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ValidationChecksServiceTest {

    private ValidationChecksService validationChecksService;
    private ValidationResult result;

    @BeforeEach
    void setup() {
        validationChecksService = new ValidationChecksService();

    }

    @Test
    void test_checkNULLThatUserOfModel() {
        //準備
        String moji = "test";
        // 実測値
        result = validationChecksService.checkSendMail(moji);
        // 期待値
        ValidationResult expected = ValidationResult.success();
        //実行
        assertThat(result, equalTo(expected));

    }

    //空文字入れてエラー返し期待
    @Test
    void sendKARAMOJI()
    {
        String x = "";
        result = validationChecksService.checkSendMail(x);
        ValidationResult expected = ValidationResult.error("email","メールアドレスが空です");
        assertThat(result,equalTo(expected));
    }

}