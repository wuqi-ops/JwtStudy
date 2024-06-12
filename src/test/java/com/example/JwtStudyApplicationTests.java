package com.example;

import com.example.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

@SpringBootTest
class JwtStudyApplicationTests {
    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println(userMapper.findUserByUserName("admin"));
    }
}
