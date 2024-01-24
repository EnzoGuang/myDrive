package com.yg.mydrive.web;

import com.yg.mydrive.entity.Files;
import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.ChunkMapper;
import com.yg.mydrive.mapper.FileMapper;
import com.yg.mydrive.mapper.UserMapper;
import com.yg.mydrive.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.yg.mydrive.service.FileService.handleDeleteFileByName;
import static com.yg.mydrive.service.FileService.handleDownloadFile;


@Controller
@RequestMapping("user")
public class UserManipulateController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    ChunkMapper chunkMapper;

    /**
     * 进行用户登录时,通过邮箱和密码进行鉴别
     * @param email
     * @param password
     * @param modelMap
     * @param session
     * @return
     */
    @PostMapping("login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        ModelMap modelMap,
                        HttpSession session) {
        User user = userMapper.findUserByEmail(email);

        if (user == null || !user.getEmail().equals(email) || !user.getPassword().equals(password)) {
            modelMap.put("error", "Incorrect email or password. Please try again.");
            return "errorLogin";
        } else {
            // 设置当前用户会话
            session.setAttribute("currentUser", user);
            modelMap.put("currentUser", user);
            return "redirect:/user/homepage";
        }
    }

    /**
     * 用户上传完文件后,返回用户主页面
     * @param modelMap
     * @param session
     * @return
     */
    @RequestMapping("homepage")
    public String homePage(ModelMap modelMap, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        modelMap.put("currentUser", user);
        List<Files> filesList = fileMapper.findFileByUserIdAndFolderId(user.getUserId(), null);
        modelMap.put("filesList", filesList);
        if (user == null) {
            return "redirect:/index";
        }
        return "homepage";
    }


    /**
     * 处理用户的文件上传
     * @param file
     * @param modelMap
     * @param session
     * @param redirectAttributes
     * @return
     */
    @PostMapping("uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             ModelMap modelMap,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/index";
        }
        ResponseEntity responseEntity = FileService.handleUploadFile(file, session, fileMapper);
        modelMap.put("uploadMessage", responseEntity.getBody());
        redirectAttributes.addFlashAttribute("uploadMessage", responseEntity.getBody());
        return "redirect:/user/homepage";
    }

    @PostMapping("uploadChunkFile")
    public String uploadChunkFile(@RequestParam("file") MultipartFile chunk,
                                  @RequestParam("fileName") String fileName,
                                  @RequestParam("index") int index,
                                  @RequestParam("fileHash") String fileHash,
                                  @RequestParam("chunkHash") String clientChunkHash,
                                  @RequestParam("totalChunks") int totalChunks,
                                  ModelMap modelMap,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) throws NoSuchAlgorithmException, IOException {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/index";
        }
        ResponseEntity responseEntity = FileService.handleUploadChunkFile(user.getUserId(), chunk, fileName, index, fileHash, clientChunkHash, totalChunks, chunkMapper, fileMapper);
        modelMap.put("uploadMessage", responseEntity.getBody());
        redirectAttributes.addFlashAttribute("uploadMessage", responseEntity.getBody());
        return "redirect:/user/homepage";
    }

    @GetMapping("download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpSession session) throws MalformedURLException {
        User user = (User) session.getAttribute("currentUser");
        return handleDownloadFile(fileName, user, fileMapper);
    }

    @GetMapping("deleteFile/{fileName:.+}")
    public String deleteFile(@PathVariable String fileName, HttpSession session, ModelMap modelMap) throws IOException {
        User user = (User) session.getAttribute("currentUser");
        int result = handleDeleteFileByName(fileName, user, fileMapper);
        if (result == 1) {
            modelMap.put("deleteMessage", fileName + " delete success!");
        } else {
            modelMap.put("deleteMessage", fileName + " delete fail");
        }
        return "redirect:/user/homepage";
    }

}
