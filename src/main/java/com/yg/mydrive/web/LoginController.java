package com.yg.mydrive.web;

import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    UserMapper userMapper;

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password) {

        User user = userMapper.findUserByEmail(email, password);
        return "homepage";
    }
}
