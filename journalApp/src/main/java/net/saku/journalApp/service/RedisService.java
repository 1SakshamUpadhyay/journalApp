package net.saku.journalApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.redis.enabled:true}")
    private boolean redisEnabled;

    public <T> T get(String key, Class<T> entityClass) {
        if (!redisEnabled) {
            return null;
        }
        try {
            Object o = redisTemplate.opsForValue().get(key);
            if (o == null) {
                return null;
            }
            return objectMapper.readValue(o.toString(), entityClass);
        } catch (Exception e) {
            log.error("Redis GET failed for key: {}", key, e);
            return null;
        }
    }

    public void set(String key, Object o, Long ttl) {
        if (!redisEnabled) {
            return;
        }
        try {
            String jsonValue = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(
                    key, jsonValue, ttl, TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("Redis SET failed for key: {}", key, e);
        }
    }
}