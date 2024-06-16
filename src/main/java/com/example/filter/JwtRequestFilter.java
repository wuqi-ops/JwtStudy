package com.example.filter;

import com.example.entity.MyUserDetails;
import com.example.entity.User;
import com.example.utils.JwtUtil;
import com.example.utils.RedisTokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author wuqi
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private RedisTokenUtil redisTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从JWT中提取用户id, 角色和权限
            Claims claims = jwtUtil.extractAllClaims(jwt);
            // id、username、password、role等用户完整信息可以从Redis中直接拿到，这里为了学习从token中取负载值，所以使用claims获取
            Long id = claims.get("id", Long.class);
            String role = claims.get("role", String.class);
            MyUserDetails userDetails = new MyUserDetails(new User(id, username, role));

            // 退出登录接口/logout，会将对应的key从Redis中删除
            // 逻辑：如果Redis中还存在对应的登录token, 才进行下一步的token校验判断
            if (redisTokenUtil.getUser(String.valueOf(id), username) != null){
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // SecurityContextHolder用于存储安全上下文信息，包括认证对象的信息。
                    // 可以使用这种方式获取存储的认证对象信息：Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } else {
                // 如果user为null，清除SecurityContext中的认证信息
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}