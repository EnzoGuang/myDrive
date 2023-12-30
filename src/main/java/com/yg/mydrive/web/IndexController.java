package com.yg.mydrive.web;

import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class IndexController {

    @Autowired
    UserMapper userMapper;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    // 进行注册的主页
    @RequestMapping("signupIndex")
    public String signupIndex() {
        return "signupIndex";
    }

    @PostMapping("signup")
    public String signup(User user) {
        userMapper.createUser(user);
        return "redirect:/index";
    }

}
