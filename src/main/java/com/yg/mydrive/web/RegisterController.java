package com.yg.mydrive.web;

import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class RegisterController {

    @Autowired
    UserMapper userMapper;

    // 进行注册的主页
    @RequestMapping("registerIndex")
    public String registerIndex() {
        return "register";
    }

    @PostMapping("registerSave")
    public String registerSave(User user, ModelMap modelMap) {
        User newUser = userMapper.findUserByEmail(user.getEmail());
        if (newUser == null) {
            userMapper.createUser(user);
            return "registerSuccess";
        } else {
            modelMap.put("error", "此邮箱已被其他用户使用，请使用别的邮箱");
            return "register";
        }
    }
}
