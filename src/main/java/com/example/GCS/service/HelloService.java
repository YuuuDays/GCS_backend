package com.example.GCS.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String returnMSG()
    {
        return "Hello,World!,こんにちは都築さん";
    }
}
