package com.example.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * @author wuqi
 */
@Component
public class BcryptUtils {
    // 加密算法
    public static String encrypt(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    // 验证加密字符串解密后是否与原password一致
    public static boolean matches(String password, String encryptedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(password, encryptedPassword);
    }

    // 生成适用于HS256算法的密钥
    public static String getKey() {
        // 生成一个适用于HS256算法的密钥
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // 将密钥编码为Base64字符串
        return Encoders.BASE64.encode(key.getEncoded());
    }

    public static void main(String[] args) {
        String encrypt = encrypt("xpt");
        System.out.println(encrypt);

        boolean matches = matches("wuqi", encrypt);
        System.out.println(matches);

        System.out.println(getKey());
    }
}
