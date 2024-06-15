package com.example.controller;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
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
            // 会自动去调用，自定义的MyUserDetailsService（实现UserDetailsService）中的loadUserByUsername方法去查询用户信息做比较，可以DEBUG跟一下
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
        // JwtRequestFilter过滤器中已经将请求的上下文信息存储到SecurityContextHolder
        // 所以这里可以直接从SecurityContextHolder中获取到请求的上下文信息，包括认证信息
        UserDetails userDetails = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
            // 其他用户信息的提取逻辑
            System.out.println(userDetails);
        }

        assert userDetails != null;
        return userDetails.toString();
    }
}
