package com.yg.mydrive.web;

import com.yg.mydrive.entity.Files;
import com.yg.mydrive.entity.Folder;
import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.ChunkMapper;
import com.yg.mydrive.mapper.FileMapper;
import com.yg.mydrive.mapper.FolderMapper;
import com.yg.mydrive.mapper.UserMapper;
import com.yg.mydrive.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.yg.mydrive.service.FileService.*;


@Controller
@RequestMapping("user")
public class UserManipulateController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    ChunkMapper chunkMapper;

    @Autowired
    FolderMapper folderMapper;

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
     * 用户退出登录
     * @param session
     * @return
     */
    @GetMapping("logout")
    public String logout(HttpSession session) {
        session.removeAttribute("currentUser");
        return "redirect:/index";
    }

    /**
     * 映射根目录访问
     * @param modelMap
     * @param session
     * @return
     */
    @RequestMapping("homepage")
    public String homepageRoot(ModelMap modelMap, HttpSession session) {
        return homePage(modelMap, session, null);
    }

    /**
     * 包含用户指定目录名的访问
     * @param modelMap
     * @param session
     * @return
     */
    @RequestMapping("homepage/{folderName}")
    public String homePage(ModelMap modelMap, HttpSession session, @PathVariable(required = false) String folderName) {
        modelMap.clear();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/index";
        }
        modelMap.put("currentUser", user);

        List<Files> filesList;
        List<Folder> foldersList;

        // 如果没有传入目录名, 默认显示根目录的所有内容
        if (folderName == null || folderName.isEmpty()) {
            filesList = fileMapper.findFileByUserIdAndFolderId(user.getUserId(), null);
            foldersList = folderMapper.getRootFolder(user.getUserId());
        } else {
            Integer folderId = folderMapper.getFolderIdByFolderName(folderName, user.getUserId());
            filesList = fileMapper.findFileByUserIdAndFolderId(user.getUserId(), folderId);
            foldersList = folderMapper.getFoldersByParentFolderIdAndUserId(folderId, user.getUserId());
        }

        modelMap.put("filesList", filesList);
        modelMap.put("foldersList", foldersList);

        return "homepage";
    }


    /**
     * 处理用户的文件上传
     * @param
     * @param modelMap
     * @param session
     * @param redirectAttributes
     * @return
     */
//    @PostMapping("uploadFile")
//    public String uploadFile(@RequestParam("file") MultipartFile file,
//                             ModelMap modelMap,
//                             HttpSession session,
//                             RedirectAttributes redirectAttributes) {
//        User user = (User) session.getAttribute("currentUser");
//        if (user == null) {
//            return "redirect:/index";
//        }
//        ResponseEntity responseEntity = handleUploadFile(file, session, fileMapper);
//        modelMap.put("uploadMessage", responseEntity.getBody());
//        redirectAttributes.addFlashAttribute("uploadMessage", responseEntity.getBody());
//        return "redirect:/user/homepage";
//    }

    @PostMapping("uploadChunkFile")
    public String uploadChunkFile(@RequestParam("file") MultipartFile chunk,
                                  @RequestParam("fileName") String fileName,
                                  @RequestParam("index") int index,
                                  @RequestParam("fileHash") String fileHash,
                                  @RequestParam("chunkHash") String clientChunkHash,
                                  @RequestParam("totalChunks") int totalChunks,
                                  @RequestParam("parentFolderName") String folderName,
                                  ModelMap modelMap,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) throws NoSuchAlgorithmException, IOException {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/index";
        }
        ResponseEntity responseEntity = handleUploadChunkFile(user.getUserId(), chunk, fileName,
                                                        index, fileHash, clientChunkHash, totalChunks, folderName,
                                                        chunkMapper, fileMapper, folderMapper);
        modelMap.put("uploadMessage", responseEntity.getBody());
        redirectAttributes.addFlashAttribute("uploadMessage", responseEntity.getBody());
        return "redirect:/user/homepage";
    }

    @GetMapping("download/{fileName:.+}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable String fileName, HttpSession session) throws MalformedURLException, UnsupportedEncodingException {
        User user = (User) session.getAttribute("currentUser");
        return handleDownloadFile(fileName, user, fileMapper, chunkMapper);
    }

    @GetMapping("deleteFile/{fileName:.+}")
    public String deleteFile(@PathVariable String fileName, HttpSession session, ModelMap modelMap) throws IOException {
        User user = (User) session.getAttribute("currentUser");
        int deleteResult = handleDeleteFileByName(fileName, user, fileMapper, chunkMapper);
        if (deleteResult == 1) {
            modelMap.put("deleteMessage", fileName + " delete success!");
        } else {
            modelMap.put("deleteMessage", fileName + " delete fail");
        }
        return "redirect:/user/homepage";
    }

    /**
     * 创建文件夹
     * @param folderName
     * @param session
     * @param modelMap
     * @return
     */
    @PostMapping("createFolder")
    public ResponseEntity<String> createFolder(@RequestParam String folderName,
                                               @RequestParam String parentFolderName,
                                               HttpSession session,
                                               ModelMap modelMap) {
        User user = (User) session.getAttribute("currentUser");
        return handleCreateFolder(folderName, parentFolderName, user, folderMapper);
    }

}
