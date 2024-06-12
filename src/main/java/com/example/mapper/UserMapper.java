package com.example.mapper;

import com.example.entity.*;
import org.apache.ibatis.annotations.Mapper;
/**
 * @author wuqi
 */
@Mapper
public interface UserMapper {
    User findUserByUserName(String userName);
}
