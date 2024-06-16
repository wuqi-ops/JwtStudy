package com.example.entity;

import com.example.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;

/**
 * @author wuqi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String role;

    public User(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
