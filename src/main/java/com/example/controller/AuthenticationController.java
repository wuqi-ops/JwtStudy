package com.example.controller;

import com.example.entity.MyUserDetails;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.utils.JwtUtil;
import com.example.utils.RedisTokenUtil;
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
import java.util.Date;
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
    private RedisTokenUtil redisTokenUtil;

    @PostMapping("/login")
    public String createAuthenticationToken(@RequestBody User user) throws Exception {
        try {
            // 会调用自定义的MyUserDetailsService（实现UserDetailsService）中的loadUserByUsername方法去查询用户信息做比较，可以DEBUG跟一下
            // 调用路径 AuthenticationManager.authenticate() -> ProviderManager.authenticate() -> AuthenticationProvider.authenticate()
            // ->AbstractUserDetailsAuthenticationProvider.authenticate() -> DaoAuthenticationProvider.loadUserByUsername()
            // ->MyUserDetailsService(自定义的实现类).loadUserByUsername()
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        } catch (AuthenticationException e) {
            throw new Exception("Incorrect username or password", e);
        }

        // 会调用自定义的MyUserDetailsService（实现UserDetailsService）中的loadUserByUsername方法去查询用户信息
        final MyUserDetails userDetails = (MyUserDetails) userDetailsService.loadUserByUsername(user.getUsername());
        // 生成JWT
        Map<String, Object> claims = new HashMap<>();
        // claims是自定义声明（负载），只是为了可以让服务端从jwt中解析出更多信息（减少查询数据库的次数），不传也可以生成jwt
        if (userDetails != null) {
            if (userDetails.getAuthorities() != null) {
                // 这里遍历权限列表，但只取遍历到到第一个权限值
                claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
            }
            claims.put("id", user.getId());

            String jwt = jwtUtil.generateToken(userDetails.getUsername(), claims);
            Date expirationDate = jwtUtil.extractExpiration(jwt);
            long expirationTime = expirationDate.getTime() - System.currentTimeMillis();

            // 创建存储到redis中的user对象
            User userSavedInRedis = new User();
            userSavedInRedis.setId(userDetails.getId());
            userSavedInRedis.setUsername(userDetails.getUsername());
            userSavedInRedis.setPassword(userDetails.getPassword());
            if (userDetails.getAuthorities() != null) {
                // 这里遍历权限列表，但只取遍历到到第一个权限值
                userSavedInRedis.setRole(userDetails.getAuthorities().iterator().next().getAuthority());
            }
            // 缓存用户信息并设置过期时间
            redisTokenUtil.cacheUser(userSavedInRedis, expirationTime);
        }else {
            throw new RuntimeException("userDetails is null");
        }

        return userDetails.toString();
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
