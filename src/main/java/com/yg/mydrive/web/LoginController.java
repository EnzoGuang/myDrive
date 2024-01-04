package com.yg.mydrive.web;

import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    UserMapper userMapper;

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, ModelMap modelMap) {

        User user = userMapper.findUserByEmail(email, password);
        if (user == null) {
            return "empty";
        }
        modelMap.put("currentUser", user);
        return "homepage";
    }
}
