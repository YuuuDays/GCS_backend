package com.example.GCS.service;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


class RegisterServiceTest extends RegisterService {

    // assertThat(actual, is(expected));
    @Test
    void TestcheckGitHubAccount(){
        assertTrue(checkGitHubAccount("tanaka"));
    }
    @Test
    void test2()
    {
        boolean result = checkGitHubAccount("tanaka");
        assertThat(result, is(true));
    }

}