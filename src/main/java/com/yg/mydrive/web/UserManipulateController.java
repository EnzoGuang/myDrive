package com.yg.mydrive.web;

import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.UserMapper;
import com.yg.mydrive.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping("user")
public class UserManipulateController {

    @Autowired
    UserMapper userMapper;

    @PostMapping("login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, ModelMap modelMap) {
        User user = userMapper.findUserByEmail(email);

        if (user == null || !user.getEmail().equals(email) || !user.getPassword().equals(password)) {
            modelMap.put("error", "Incorrect email or password. Please try again.");
            return "errorLogin";
        } else {
            String homeDir= System.getProperty("user.home");
            String currentDir = System.getProperty("user.dir");
            modelMap.put("homeDir", homeDir);
            modelMap.put("currentDir", currentDir);
            modelMap.put("currentUser", user);
            return "homepage";
        }
    }

    @PostMapping("uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file, ModelMap modelMap) {
        ResponseEntity responseEntity = FileService.handleUploadFile(file);
        modelMap.put("uploadMessage", responseEntity.getBody());
        return "homepage";
    }
}
