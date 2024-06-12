package com.example.controller;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public String createAuthenticationToken(@RequestBody User user) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(user.getUsername());

        // 生成JWT
        Map<String, Object> claims = new HashMap<>();
        // 假设所有用户都为admin
        // claims是自定义声明，只是为了可以让服务端从jwt中解析出更多信息（减少查询数据库的次数），不传也可以生成jwt
        claims.put("role", "admin");
        return jwtUtil.generateToken(userDetails.getUsername(), claims);
    }

    @PostMapping("/doSomething")
    public String doSomething(@RequestBody User user) throws Exception {
        System.out.println(user);

        // 生成JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        return "doSomething";
    }

    @PostMapping("/hello")
    public String hello(@RequestBody User user) {
        User userByUserName = userMapper.findUserByUserName(user.getUsername());
        System.out.println(userByUserName);
        return "hello";
    }
}
