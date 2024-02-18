package com.yg.mydrive.web;

import com.yg.mydrive.entity.Files;
import com.yg.mydrive.entity.Folder;
import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
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
    FileChunkMapper fileChunkMapper;

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
    @RequestMapping("homepage/{folderId}")
    public String homePage(ModelMap modelMap, HttpSession session, @PathVariable(required = false) Integer folderId) {
        modelMap.clear();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/index";
        }
        modelMap.put("currentUser", user);

        List<Files> filesList;
        List<Folder> foldersList;

        // 如果没有传入目录名, 默认显示根目录的所有内容
        if (folderId == null ) {
            filesList = fileMapper.findFileByUserIdAndFolderId(user.getUserId(), null);
            foldersList = folderMapper.getRootFolder(user.getUserId());
        } else {
            filesList = fileMapper.findFileByUserIdAndFolderId(user.getUserId(), folderId);
            foldersList = folderMapper.getFoldersByParentFolderIdAndUserId(folderId, user.getUserId());
        }

        modelMap.put("filesList", filesList);
        modelMap.put("foldersList", foldersList);

        return "homepage";
    }


//    @PostMapping("uploadChunkFile")
//    public String uploadChunkFile(@RequestParam("file") MultipartFile chunk,
//                                  @RequestParam("fileName") String fileName,
//                                  @RequestParam("index") int index,
//                                  @RequestParam("fileHash") String fileHash,
//                                  @RequestParam("chunkHash") String clientChunkHash,
//                                  @RequestParam("totalChunks") int totalChunks,
//                                  @RequestParam("parentFolderName") String folderName,
//                                  ModelMap modelMap,
//                                  HttpSession session,
//                                  RedirectAttributes redirectAttributes) throws NoSuchAlgorithmException, IOException {
//        User user = (User) session.getAttribute("currentUser");
//        if (user == null) {
//            return "redirect:/index";
//        }
//        ResponseEntity responseEntity = handleUploadChunkFile(user.getUserId(), chunk, fileName,
//                                                        index, fileHash, clientChunkHash, totalChunks, folderName,
//                                                        chunkMapper, fileMapper, fileChunkMapper, folderMapper);
//        modelMap.put("uploadMessage", responseEntity.getBody());
//        redirectAttributes.addFlashAttribute("uploadMessage", responseEntity.getBody());
//        return "redirect:/user/homepage";
//    }


    @PostMapping("uploadChunk")
    public String uploadChunk(@RequestParam("chunk") MultipartFile chunk,
                                              @RequestParam("fileId") Integer fileId,
                                              @RequestParam("parentFolderId") String parentFolderId,
                                              @RequestParam("chunkIndex") Integer chunkIndex,
                                              @RequestParam("chunkHash") String chunkHash,
                                              @RequestParam("totalChunks") Integer totalChunks,
                                              HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/index";
        }
        if (parentFolderId.equals("null")) {
            handleChunks(chunk, fileId, null, chunkIndex, chunkHash, totalChunks, user, fileMapper, chunkMapper, fileChunkMapper);
        } else {
            handleChunks(chunk, fileId, Integer.parseInt(parentFolderId), chunkIndex, chunkHash, totalChunks, user, fileMapper, chunkMapper, fileChunkMapper);
        }

        String redirectUrl = "redirect:/user/homepage";
        if (parentFolderId != null) {
            redirectUrl += "/" + parentFolderId;
        }
        return redirectUrl;
    }

    /**
     * 进行文件上传初始化操作,返回该文件的id值
     * @param fileName
     * @param fileHash
     * @param totalChunks
     * @param parentFolderId
     * @return
     */
    @PostMapping("initializeFileUpload")
    public ResponseEntity<String> initializeFileUpload(@RequestParam String fileName,
                                        @RequestParam String fileHash,
                                        @RequestParam Integer totalChunks,
                                        @RequestParam(value = "parentFolderId", required = false) Integer parentFolderId,
                                        HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Integer fileId = initializeUpload(fileName, fileHash, totalChunks, user, parentFolderId, fileMapper);
        if (fileId != null) {
            return ResponseEntity.ok().body(fileId.toString());
        }
        return ResponseEntity.internalServerError().body("服务端文件初始化操作失败");
    }

    /**
     * 判断分片是否已经在服务端存储,如果分片存在需要增加该分片的引用次数
     * @param chunkHash
     * @return
     */
    @PostMapping("checkChunkExist")
    public ResponseEntity<String> checkChunkExist(@RequestParam("chunkHash") String chunkHash,
                                                  @RequestParam("fileId") Integer fileId,
                                                  @RequestParam("chunkIndex") Integer chunkIndex) {
        Integer chunkId= checkChunkHashIfExists(chunkHash, fileId, chunkIndex, chunkMapper, fileChunkMapper);
        return ResponseEntity.ok(chunkId != null ? "true": "false");
    }

    @PostMapping("updateFileSize")
    public ResponseEntity<String> updateFileSize(@RequestParam("fileId") Integer fileId) {
        Long size = updateFileSizeById(fileId, fileMapper, chunkMapper, fileChunkMapper);
        return ResponseEntity.ok().body(size + " ");
    }

    @GetMapping("download/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable Integer fileId, HttpSession session) throws UnsupportedEncodingException {
        User user = (User) session.getAttribute("currentUser");
        return handleDownloadFile(fileId, user, fileMapper, chunkMapper, fileChunkMapper);
    }

//    @GetMapping("deleteFile/{fileId}")
//    public String deleteFile(@PathVariable String fileId, HttpSession session, ModelMap modelMap) throws IOException {
//        User user = (User) session.getAttribute("currentUser");
//        int deleteResult = handleDeleteFileByName(fileName, user, fileMapper, chunkMapper);
//        if (deleteResult == 1) {
//            modelMap.put("deleteMessage", fileName + " delete success!");
//        } else {
//            modelMap.put("deleteMessage", fileName + " delete fail");
//        }
//        return "redirect:/user/homepage";
//    }

    /**
     * 创建文件夹
     * @param folderName
     * @param session
     * @param modelMap
     * @return
     */
    @PostMapping("createFolder")
    public ResponseEntity<String> createFolder(@RequestParam String folderName,
                                               @RequestParam Integer parentFolderId,
                                               HttpSession session,
                                               ModelMap modelMap) {
        User user = (User) session.getAttribute("currentUser");
        return handleCreateFolder(folderName, parentFolderId, user, folderMapper);
    }

}
