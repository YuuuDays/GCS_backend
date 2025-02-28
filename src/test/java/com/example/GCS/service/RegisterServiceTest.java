package com.example.GCS.service;

import com.example.GCS.model.User;
import com.example.GCS.repository.RegisterRepository;
import com.example.GCS.utils.ResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegisterServiceTest {

    private RegisterService registerService;

    @Mock
    private RegisterRepository registerRepository;
    @Mock
    private ValidationChecksService validationChecksService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        registerService = new RegisterService(validationChecksService,registerRepository);
    }
    @Test
    void testGoogleId_NUll_check()
    {
        User user = new User();
        ResponseEntity<Map<String,Object>> result = registerService.register(user);
        ResponseEntity<Map<String,Object>> expected = new ResponseBuilder()
                .success(false)
                .addError("googleId","googleIdが不正です。もう一度最初からやり直してください")
                .build();

    }
}
