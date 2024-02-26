package com.yg.mydrive.web;

import com.yg.mydrive.entity.FileVersion;
import com.yg.mydrive.entity.Files;
import com.yg.mydrive.entity.Folder;
import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    FileVersionMapper fileVersionMapper;

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

    /**
     * 上传分片
     * @param chunk
     * @param fileId
     * @param parentFolderId
     * @param chunkIndex
     * @param chunkHash
     * @param totalChunks
     * @param session
     * @return
     */
    @PostMapping("uploadChunk")
    public String uploadChunk(@RequestParam("chunk") MultipartFile chunk,
                                              @RequestParam("fileId") Integer fileId,
                                              @RequestParam("parentFolderId") String parentFolderId,
                                              @RequestParam("chunkIndex") Integer chunkIndex,
                                              @RequestParam("chunkHash") String chunkHash,
                                              @RequestParam("totalChunks") Integer totalChunks,
                                              @RequestParam(required = false, value = "versionId") Integer versionId,
                                              HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/index";
        }
        if (parentFolderId.equals("null")) {
            handleChunks(chunk, fileId, chunkIndex, chunkHash, totalChunks, versionId, fileMapper, chunkMapper, fileChunkMapper);
        } else {
            handleChunks(chunk, fileId, chunkIndex, chunkHash, totalChunks, versionId, fileMapper, chunkMapper, fileChunkMapper);
        }

        String redirectUrl = "redirect:/user/homepage";
        if (parentFolderId != null) {
            redirectUrl += "/" + parentFolderId;
        }
        return redirectUrl;
    }

    /**
     * 进行文件上传初始化操作,返回该文件的id值
     *
     * @param fileName
     * @param totalChunks
     * @param parentFolderId
     * @return
     */
    @PostMapping("initializeFileUpload")
    public ResponseEntity<Map<String, Object>> initializeFileUpload(@RequestParam String fileName,
                                                       @RequestParam Integer totalChunks,
                                                       @RequestParam(value = "parentFolderId", required = false) Integer parentFolderId,
                                                       @RequestParam(value = "versionControlEnabled", required = false, defaultValue = "false") Boolean versionControlEnabled,
                                                       @RequestParam(value = "updateVersion", required = false, defaultValue = "false") Boolean updateVersion,
                                                       HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Map<String, Object> responseBody = new HashMap<>();
        Files file = initializeUpload(fileName, totalChunks, user, parentFolderId, versionControlEnabled, fileMapper, fileVersionMapper);
        if (file.getFileId() != null) {
            responseBody.put("fileId", file.getFileId());
            Integer versionId = file.getCurrentVersionId();
            responseBody.put("versionId", versionId != null ? versionId.toString() : null);
            return ResponseEntity.ok().body(responseBody);
        } else {
            responseBody.put("error", "服务端文件初始化操作失败");
            return ResponseEntity.internalServerError().body(responseBody);
        }
    }

    @PostMapping("fileNewVersionInitialize")
    public ResponseEntity<Map<String, Object>> uploadFileNewVersion(@RequestParam("fileId") Integer fileId,
                                                       HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Map<String, Object> responseBody = new HashMap<>();
        Files file = fileMapper.getFileById(fileId, user.getUserId());
        Integer versionNumber = fileVersionMapper.getMaxVersionNumber(fileId);
        Integer newVersionNumber = versionNumber + 1;

        // 生成新的file-version记录
        FileVersion fileVersion = new FileVersion(file.getFileId(), newVersionNumber);
        fileVersionMapper.createNewFileVersion(fileVersion);
        // 获得新生成记录了的id
        Integer currentFileVersionId = fileVersion.getFileVersionId();
        // 更新文件记录的当前版本id
        fileMapper.updateVersionId(file.getFileId(), currentFileVersionId);
        responseBody.put("fileVersionId", currentFileVersionId);
        return ResponseEntity.ok().body(responseBody);
    }

    /**
     * 为已上传文件开启版本控制
     * @param fileId
     * @param httpSession
     * @return
     */
    @PostMapping("openVersionControl")
    public ResponseEntity<String> openVersionControl(@RequestParam("fileId") Integer fileId,
                                                     HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("currentUser");
        Integer versionId = handleOpenVersionControl(fileId, user, fileMapper, fileChunkMapper, fileVersionMapper);
        return ResponseEntity.ok().body("versionId: " + versionId);
    }

    @PostMapping("fetchFileVersions")
    public ResponseEntity<List<FileVersion>> fetchFileVersions(@RequestParam("fileId") Integer fileId) {
        List<FileVersion> fileVersionList = fileVersionMapper.getAllVersionByFileId(fileId);
        return ResponseEntity.ok().body(fileVersionList);
    }

    /**
     * 判断分片是否已经在服务端存储,如果分片存在需要增加该分片的引用次数
     * @param chunkHash
     * @return
     */
    @PostMapping("checkChunkExist")
    public ResponseEntity<String> checkChunkExist(@RequestParam("chunkHash") String chunkHash,
                                                  @RequestParam("fileId") Integer fileId,
                                                  @RequestParam("chunkIndex") Integer chunkIndex,
                                                  @RequestParam(required = false, value = "versionId") Integer versionId) {
        Integer chunkId= checkChunkHashIfExists(chunkHash, fileId, chunkIndex, versionId, chunkMapper, fileChunkMapper);
        return ResponseEntity.ok(chunkId != null ? "true": "false");
    }

    /**
     * 更新文件大小
     * @param fileId
     * @return
     */
    @PostMapping("updateFileSize")
    public ResponseEntity<String> updateFileSize(@RequestParam("fileId") Integer fileId,
                                                 HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Long size = updateFileSizeById(fileId, user.getUserId(), fileMapper, chunkMapper, fileChunkMapper);
        return ResponseEntity.ok().body(size + " ");
    }

    /**
     * 根据文件id下载文件
     * @param fileId
     * @param session
     * @return
     * @throws UnsupportedEncodingException
     */
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
     * 展示回收站的页面
     * @param session
     * @param modelMap
     * @return
     */
    @GetMapping("recyclePage")
    public String recyclePage(HttpSession session, ModelMap modelMap) {
        User user = (User) session.getAttribute("currentUser");
        List<Files> softDeleteFiles = fileMapper.findFilesNotInDeletedFolders(user.getUserId());
        List<Folder> softDeleteFolders = folderMapper.findFoldersNotInDeletedFolders(user.getUserId());

        modelMap.put("softDeleteFiles", softDeleteFiles);
        modelMap.put("softDeleteFolders", softDeleteFolders);

        return "recycle";
    }

    /**
     * 软删除, 将删除项放入回收站
     * @param itemId
     * @param itemType
     * @param status
     * @param session
     * @return
     */
    @PostMapping("softDeleteOrRecoverItem")
    public ResponseEntity<String> softDeleteOrRecoverItem(@RequestParam("itemId") Integer itemId,
                                                 @RequestParam("itemType") String itemType,
                                                 @RequestParam("status") String status,
                                                 HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (itemType.equalsIgnoreCase(FILE_TYPE)) {
            handleUpdateFileStatus(itemId, user, status, fileMapper);
        } else if(itemType.equalsIgnoreCase(FOLDER_TYPE)) {
            handleUpdateFolderStatus(itemId, user, status, fileMapper, folderMapper);
        }
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    /**
     * 创建文件夹
     * @param folderName
     * @param session
     * @return
     */
    @PostMapping("createFolder")
    public ResponseEntity<String> createFolder(@RequestParam String folderName,
                                               @RequestParam(value = "parentFolderId", required = false) Integer parentFolderId,
                                               HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return handleCreateFolder(folderName, parentFolderId, user, folderMapper);
    }

    /**
     * 获得当前用户的所有目录
     * @param session
     * @return
     */
    @GetMapping("getAllFolders")
    public ResponseEntity<List<Folder>> getAllFolders(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        List<Folder> folders = folderMapper.getAllFoldersByUserId(user.getUserId());
        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    /**
     * 对用户文件进行重命名
     * @param itemId
     * @param newItemName
     * @param itemType
     * @param session
     * @return
     */
    @PostMapping("renameItem")
    public ResponseEntity<String> renameItem(@RequestParam("itemId") Integer itemId,
                                             @RequestParam("newItemName") String newItemName,
                                             @RequestParam("itemType") String itemType,
                                             HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return handleRenameItem(itemId, newItemName, itemType, user, fileMapper, folderMapper);
    }

    /**
     * 进行项目(文件或文件夹)的移动
     * @param itemId
     * @param itemType
     * @param targetFolderId
     * @param session
     * @return
     */
    @PostMapping("moveItem")
    public ResponseEntity<String> moveItem(@RequestParam Integer itemId,
                                           @RequestParam String itemType,
                                           @RequestParam(value = "targetFolderId", required = false) Integer targetFolderId,
                                           HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return handleMoveItem(itemId, itemType, targetFolderId, user, fileMapper, folderMapper);
    }

    /**
     * 用户在浏览器输入分享链接,返回分享页面的视图
     * @param data
     * @param modelMap
     * @return
     */
    @GetMapping("sharePage")
    public String shareFile(@RequestParam("data") String data, ModelMap modelMap) {
        Files file = handleShare(data, fileMapper);
        modelMap.put("data", data);
        if (file != null) {
            modelMap.put("sharingFile", file);
        } else {
            modelMap.put("sharingError", "sharing file not exist");
        }
        return "share";
    }

    /**
     * 在分享页面进行用户登录
     * @param useremail
     * @param password
     * @param data
     * @param session
     * @return
     */
    @PostMapping("sharePageLogin")
    @ResponseBody
    public Map<String, String> sharePageLogin(@RequestParam("useremail") String useremail,
                                              @RequestParam("password") String password,
                                              @RequestParam(required = false, value ="data") String data,
                                              HttpSession session) {
        User user = userMapper.findUserByEmail(useremail);

        Map<String, String> response = new HashMap<>();
        if (user == null || !user.getPassword().equals(password)) {
            response.put("status", "error");
            response.put("message", "Incorrect email or password. Please try again.");
        } else {
            // 设置当前用户会话
            session.setAttribute("currentUser", user);
            response.put("status", "success");
        }
        return response;
    }

    /**
     * 处理分享文件的保存功能
     * @param originalFileId
     * @param originalUserId
     * @param session
     * @return
     */
    @PostMapping("saveShareFile")
    public ResponseEntity<String> saveShareFile(@RequestParam("originalFileId") Integer originalFileId,
                                                @RequestParam("originalUserId") Integer originalUserId,
                                                @RequestParam(required = false, value = "folderIdString") String folderIdString,
                                                HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Integer folderId;
        if (folderIdString != null && !folderIdString.equalsIgnoreCase("null")) {
            folderId = Integer.valueOf(folderIdString);
        } else {
            folderId = null;
        }
        return handleSaveShareFile(originalFileId, originalUserId, folderId, user, fileMapper, chunkMapper, fileChunkMapper);
    }

    /**
     * 生成分享链接
     * @param fileId
     * @param session
     * @return
     */
    @GetMapping("generateShareLink/{fileId}")
    public ResponseEntity<String> generateShareLink(@PathVariable Integer fileId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");

        String data = "fileId:" + fileId + "|" + "userId:" + user.getUserId();
        String encode = encodeBase64(data);
        String shareLink = "http://localhost:8080/user/sharePage?data=" + encode;
        return ResponseEntity.ok().body(shareLink);

    }

}
