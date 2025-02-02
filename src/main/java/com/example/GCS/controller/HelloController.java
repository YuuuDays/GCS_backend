package com.example.GCS.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "https://githubcontributesystem.netlify.app") // React のURL
@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello(){
        return "Hello,World!,こんにちは都築さん";
    }
}
