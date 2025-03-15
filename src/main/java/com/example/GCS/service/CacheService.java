package com.example.GCS.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

// 概要:Redisキャッシュ管理
@Service
public class CacheService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "github_cache:";
    // キャッシュからデータ取得
    public Map<String,Object> getCache(String UserName)
    {
        String cache = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + UserName);
        if (cache != null) {
            try {
                return objectMapper.readValue(cache, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    // キャッシュを更新
    public void setCache(String username, Map<String, Object> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + username, jsonData, Duration.ofMinutes(5));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
