package com.example.GCS.service;

import com.example.GCS.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationChecksServiceTest {

    private ValidationChecksService validationChecksService;

    @BeforeEach
    void setup()
    {
        validationChecksService = new ValidationChecksService();
    }

    @Test
    void test_checkNULLThatUserOfModel()
    {
        //準備
        User user = new User();
        boolean result = validationChecksService.checkNULLThatUserOfModel(user);
        assertEquals(true,result);
    }
}