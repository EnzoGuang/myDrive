package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    void createUser(User user);

    User findUserByEmail(@Param("email") String email);
}
