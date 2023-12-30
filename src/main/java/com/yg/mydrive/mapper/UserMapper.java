package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void createUser(User user);
}
