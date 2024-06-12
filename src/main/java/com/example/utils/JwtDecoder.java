package com.example.utils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtDecoder {

    // JWT由头部、负载、签名三部分组成。JWT的头部（Header）和载荷（Payload）是以Base64Url编码的形式存在的，
    // 并不是加密的，而签名只能验证jwt的完整性（防篡改）。这意味着任何人只要拥有JWT，就可以解码并查看其头部和载荷的内容。
    // 因为负载不安全，所以一般不将用户密码等隐私信息编码到jwt中（一般只保留userId和userName）。可以使用userId作为Key,将用户完整信息存入redis
    private static ObjectMapper objectMapper = new ObjectMapper();

    // 解码JWT内容
    public static Map<String, Object> decodeJwt(String jwt) throws Exception {
        // 分割JWT为三个部分
        String[] parts = jwt.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        // 解码头部
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
        Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);

        // 解码载荷
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);

        // 将头部和载荷放到结果Map中
        Map<String, Object> result = new HashMap<>();
        result.put("header", header);
        result.put("payload", payload);

        return result;
    }

    public static void main(String[] args) {
        try {
            String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";
            Map<String, Object> decodedJwt = decodeJwt(jwt);
            System.out.println("Header: " + decodedJwt.get("header"));
            System.out.println("Payload: " + decodedJwt.get("payload"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
