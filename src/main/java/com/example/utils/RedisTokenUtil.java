package com.example.utils;

import com.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author wuqi
 */
@Component
public class RedisTokenUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // Redis中 user详细信息key格式：user:token:#{userId}:#{username}
    // Redis中 user详细信息value为hash
    private static final String USER_KEY_PREFIX = "user:token:";

    public void cacheUser(User user, long expirationTime) throws RuntimeException {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        if (user != null && user.getId() != null && user.getUsername() != null) {
            String key = USER_KEY_PREFIX + user.getId() + ":" + user.getUsername();
            hashOps.put(key, "id", user.getId().toString());
            hashOps.put(key, "username", user.getUsername());
            hashOps.put(key, "password", user.getPassword());
            hashOps.put(key, "role", user.getRole());
            redisTemplate.expire(key, expirationTime, TimeUnit.MILLISECONDS);
        } else {
            throw new RuntimeException("userId or username is null");
        }
    }

    public User getUser(String userId, String username) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String key = USER_KEY_PREFIX + userId + ":" + username;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            User user = new User();
            user.setId(Long.parseLong(hashOps.get(key, "id")));
            user.setUsername(hashOps.get(key, "username"));
            user.setPassword(hashOps.get(key, "password"));
            user.setRole(hashOps.get(key, "role"));
            return user;
        }
        return null;
    }

    public void deleteUser(String userId, String username) {
        String key = USER_KEY_PREFIX + userId + ":" + username;
        redisTemplate.delete(key);
    }
}
