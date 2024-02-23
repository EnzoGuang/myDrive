package com.yg.mydrive.service;

import com.yg.mydrive.entity.*;
import com.yg.mydrive.mapper.ChunkMapper;
import com.yg.mydrive.mapper.FileChunkMapper;
import com.yg.mydrive.mapper.FileMapper;
import com.yg.mydrive.mapper.FolderMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileService {
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_DELETED = "deleted";

    public static final String FILE_TYPE = "file";
    public static final String FOLDER_TYPE = "folder";
    private static final File UPLOAD_DIR = Utils.join(System.getProperty("user.dir"), "uploads");
    private static Map<Integer, ChunkFileStatus> statusMap = new ConcurrentHashMap<>();

    /**
     * 获得上传文件夹的路径, 若该路径不存在,则进行创建
     */
    private static File getUploadDir() {
        if (!UPLOAD_DIR.exists()) {
            UPLOAD_DIR.mkdir();
        }
        return UPLOAD_DIR;
    }


    /**
     * 处理上传到的文件分片
     * @param chunk
     * @param fileId
     * @param parentFolderId
     * @param chunkIndex
     * @param clientChunkHash
     * @param totalChunks
     * @param user
     * @param fileMapper
     * @param chunkMapper
     * @param fileChunkMapper
     * @return
     */
    public static ResponseEntity<String> handleChunks(MultipartFile chunk, Integer fileId, Integer parentFolderId,
                                                      Integer chunkIndex, String clientChunkHash,
                                                      Integer totalChunks, User user,
                                                      FileMapper fileMapper,
                                                      ChunkMapper chunkMapper,
                                                      FileChunkMapper fileChunkMapper) {
        try {
            // 判断文件在传输过程中是否被篡改
            String chunkHash = getHashOfFile(chunk);
            if (!chunkHash.equals(clientChunkHash)) {
                return ResponseEntity.badRequest().body("fileId: " + fileId + " Chunk index: " + chunkIndex + " hash mismatch");
            }
            ChunkFileStatus chunkFileStatus = statusMap.computeIfAbsent(fileId, k -> new ChunkFileStatus(fileId, totalChunks));

            // 保存分片到文件系统中,该函数同时将分片记录插入分片表中，返回该分片记录的id
            Integer chunkId = saveChunkToUploadDir(chunk, clientChunkHash, chunkMapper);
            if (chunkId != null) {
                // 标记分片上传成功
                chunkFileStatus.markChunkAsUploaded(chunkIndex, chunkId, chunk.getSize());
            }

            // 向file-chunk关联表中添加记录
            FileChunk fileChunk = new FileChunk(fileId, chunkId, chunkIndex, getTime());
            Integer fileChunkId = fileChunkMapper.insertFileChunk(fileChunk);

            if (chunkFileStatus.isUploadComplete()) {
                // 文件的所有分片都上传完毕后,更新文件表添加该文件的大小
                fileMapper.updateFileSize(fileId, chunkFileStatus.getTotalSize());
                return ResponseEntity.ok().body("FileId: " + fileId + " upload successful");
            }
            return ResponseEntity.ok().body("FileId: " + fileId + " Chunk: " + chunkIndex + " uploaded successful");
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 进行文件上传初始化操作,返回该文件的id值
     * @param fileName
     * @param fileHash
     * @param totalChunks
     * @param user
     * @param parentFolderId
     * @param fileMapper
     * @return
     */
    public static Integer initializeUpload(String fileName,
                                           String fileHash,
                                           Integer totalChunks,
                                           User user,
                                           Integer parentFolderId,
                                           FileMapper fileMapper) {
        Files file = new Files(fileName, fileHash, totalChunks, parentFolderId, user.getUserId(), getTime());
        fileMapper.initializeFile(file);
        return file.getFileId();
    }

    /**
     * 通过chunkHash判断该分片是否已经存储在服务端,如果已经存在需要增加该分片的引用次数,并更新file-chunk表
     * @param chunkHash
     * @param chunkMapper
     * @return
     */
    public static Integer checkChunkHashIfExists(String chunkHash, Integer fileId, Integer chunkIndex,
                                                 ChunkMapper chunkMapper, FileChunkMapper fileChunkMapper) {
        Integer chunkId = chunkMapper.checkChunkExistsByHash(chunkHash);
        if (chunkId != null) {

            // 增加分片的引用次数
            chunkMapper.updateReferenceCountByHash(chunkId, chunkHash);

            // 更新file-chunk表
            fileChunkMapper.insertFileChunk(new FileChunk(fileId, chunkId, chunkIndex, getTime()));
            return chunkId;
        }
        return null;
    }

    /**
     * 将分片保存到文件系统中,同时将该分片记录插入分片表中,返回该分片记录的主键id值
     * @param chunk
     * @param chunkHash
     * @param chunkMapper
     * @throws IOException
     */
    private static Integer saveChunkToUploadDir(MultipartFile chunk, String chunkHash, ChunkMapper chunkMapper) throws IOException {
        // 通过分片的hash值获得存储路径
        File chunkStoragePath = getDirPathOfChunkToSave(chunkHash);

        // 将分片进行存储
        chunk.transferTo(chunkStoragePath);

        // 将该分片记录插入分片表中
        Chunk chunkRecord = new Chunk(chunkHash, chunk.getSize(), getTime());
        chunkMapper.insertChunk(chunkRecord);

        // 返回该记录的自增主键
        return chunkRecord.getChunkId();
    }

    /**
     * 获得分片的存储路径,用于上传分片,采用和git相同的存储策略,hash值的前两位作为存储目录,剩余位作为分片名
     * @param chunkHash
     * @return
     */
    private static File getDirPathOfChunkToSave(String chunkHash) {
        File chunkDir =  Utils.join(getUploadDir(), chunkHash.substring(0, 2));
        if (!chunkDir.exists()) {
            chunkDir.mkdirs();
        }
        return Utils.join(chunkDir, chunkHash.substring(2));
    }

    /**
     * 获得分片的存储路径,用于下载或删除分片
     * @param chunkHash
     * @return
     */
    private static Path getDirPathOfChunkToDownloadOrDelete(String chunkHash) {
        File chunkDir = Utils.join(getUploadDir(), chunkHash.substring(0, 2));
        return Utils.join(chunkDir, chunkHash.substring(2)).toPath();
    }

    /**
     * 计算文件的SHA-256的hash值
     * @param file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private static String getHashOfFile(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashOfFile = digest.digest(file.getBytes());
        return bytesToHex(hashOfFile);
    }

    /**
     * 将字节数组转换成十六进制字符串
     * @param content
     */
    private static String bytesToHex(byte[] content) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte temp: content) {
            stringBuilder.append(String.format("%02x", temp));
        }
        return stringBuilder.toString();

    }

    /**
     * 返回当前时间,格式与MySql的Datetime相同
     */
    private static String getTime() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String resultTime = time.format(formatter);
        return resultTime;
    }

    public static Long updateFileSizeById(Integer fileId, Integer userId, FileMapper fileMapper, ChunkMapper chunkMapper, FileChunkMapper fileChunkMapper) {
        Files currentFile = fileMapper.getFileById(fileId, userId);
        if (currentFile.getFileSize() == 0) {
            List<Integer> allChunksId = fileChunkMapper.getAllChunksId(currentFile.getFileId(), currentFile.getCurrentVersionId());
            long fileSize = 0;
            for (Integer chunkId: allChunksId) {
                fileSize += chunkMapper.getChunkSize(chunkId);
            }
            fileMapper.updateFileSize(fileId, fileSize);
            return fileSize;
        }
        return null;
    }


    public static ResponseEntity<StreamingResponseBody> handleDownloadFile(Integer fileId,
                                                                           User user,
                                                                           FileMapper fileMapper,
                                                                           ChunkMapper chunkMapper,
                                                                           FileChunkMapper fileChunkMapper) throws UnsupportedEncodingException {
        Files file = fileMapper.getFileById(fileId, user.getUserId());
        // 获得所有分片的路径
        List<Path> chunkPaths = getChunkPaths(file.getFileId(), file.getCurrentVersionId(), chunkMapper, fileChunkMapper);

        StreamingResponseBody responseBody = outputStream -> {
            for (Path chunkPath: chunkPaths) {
                if (java.nio.file.Files.exists(chunkPath)) {
                    try (InputStream inputStream = new BufferedInputStream(java.nio.file.Files.newInputStream(chunkPath))) {

                        // 设置缓冲区1MB
                        byte[] buffer = new byte[1024 * 1024];

                        int length;
                        while ((length = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                    } catch (IOException e) {
                        System.out.println(e + "合并分片出错");
                    }
                }
            }
        };

        HttpHeaders headers = new HttpHeaders();

        // 编码文件名,若文件名含有中文或其他特殊字符不进行编码,文件名会出错
        String encodedFileName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8);

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    /**
     * 获得该文件所有分片的路径,通过index进行排序从小到大
     * @param fileId
     * @param versionId
     * @param chunkMapper
     * @param fileChunkMapper
     * @return
     */
    public static List<Path> getChunkPaths(Integer fileId, Integer versionId, ChunkMapper chunkMapper, FileChunkMapper fileChunkMapper) {
        // 获得所有的分片的id值, 结果按照分片序号index排列,从小到大
        List<Integer> chunksId = fileChunkMapper.getAllChunksId(fileId, versionId);

        // 存放所有分片的路径
        List<Path> chunkPaths = new ArrayList<>();

        for (Integer id: chunksId) {
            // 通过分片id查找分片hash
            String chunkHash = chunkMapper.getChunkHashByFileId(id);
            // 获得路径
            chunkPaths.add(getDirPathOfChunkToDownloadOrDelete(chunkHash));
        }
        return chunkPaths;
    }

    /**
     * 通过文件名和用户id获得该文件的hash值
     * @param fileName
     * @param user
     * @param fileMapper
     * @return
     */
//    private static String getFileHash(String fileName, User user, FileMapper fileMapper) {
//        return fileMapper.getFileHashByFileNameAndUserId(fileName, user.getUserId());
//    }

    /**
     * 通过文件名和用户id获得该文件的hash值
     * @param fileName
     * @param user
     * @param fileMapper
     * @return
     */
//    private static Integer getFileId(String fileName, User user, FileMapper fileMapper) {
//        return fileMapper.getFileIdByFileNameAndUserId(fileName, user.getUserId());
//    }

    /**
     * 通过文件名删除文件,包含删除文件记录和分片记录,并在文件系统中删除文件
      * @param fileName
     * @param user
     * @param fileMapper
     * @param chunkMapper
     * @return
     */
    public static int handleDeleteFileByName(String fileName, User user, FileMapper fileMapper, ChunkMapper chunkMapper) {
//        String fileHash = getFileHash(fileName, user, fileMapper);
//        Integer fileId = getFileId(fileName, user, fileMapper);
//
//        // 通过fileId和fileHash获得该文件的所有分片chunks
//        List<Chunk> chunks = chunkMapper.getAllChunksByFileHash(fileHash);
//
//        // 用于记录当前被已经被删除chunk个数
//        int deletedChunks = 0;
//
//        for (Chunk chunk: chunks) {
//            File chunkPathToDelete = getDirPathOfChunkToDownloadOrDelete(chunk.getChunkHash());
//            if (chunkPathToDelete.exists()) {
//                // 在文件系统中删除该分片
//                chunkPathToDelete.delete();
//
//                // 如果该分片所处目录为空的话,删除该目录
//                File parentDir = new File(chunkPathToDelete.getParent());
//                if (parentDir.isDirectory()) {
//                    File[] files = parentDir.listFiles();
//                    if (files != null && files.length == 0) {
//                        parentDir.delete();
//                    }
//                }
//
//                // 删除分片表中的记录
//                int result = chunkMapper.deleteChunkById(chunk.getChunkId());
//                if (result == 1) {
//                    deletedChunks++;
//                }
//            }
//        }
//        if (deletedChunks == chunks.size()) {
//            return fileMapper.deleteFileByFileNameAndUserId(fileName, user.getUserId());
//        }
        return 0;
    }

//    public static ResponseEntity<String> handleSoftDeleteItem(Integer itemId,
//                                                              String status,
//                                                              User user,
//                                                              FileMapper fileMapper,
//                                                              FolderMapper folderMapper) {
//    }
//
//    public static ResponseEntity<String> handleSoftDeleteItem(Integer itemId,
//                                                              Integer parentFolderId, // 如果是单纯删除文件的话才需要此参数
//                                                              String status,
//                                                              User user,
//                                                              FileMapper fileMapper,
//                                                              FolderMapper folderMapper) {
//
//    }

    /**
     * 更新文件的status
     * @param fileId
     * @param user
     * @param status
     * @param fileMapper
     */
    public static void handleUpdateFileStatus(Integer fileId, User user, String status, FileMapper fileMapper) {
        if (status.equalsIgnoreCase(STATUS_ACTIVE)) {
            softRecoverFile(fileId, user, fileMapper);
        } else if(status.equalsIgnoreCase(STATUS_DELETED)) {
            softDeleteFile(fileId, user, fileMapper);
        }
    }

    /**
     * 设置文件status 为 'deleted'
     * @param fileId
     * @param user
     * @param fileMapper
     */
    private static void softDeleteFile(Integer fileId, User user, FileMapper fileMapper) {
        fileMapper.updateFileStatus(fileId, user.getUserId(), STATUS_DELETED);
    }

    /**
     * 设置文件status 为 'active'
     * @param fileId
     * @param user
     * @param fileMapper
     */
    private static void softRecoverFile(Integer fileId, User user, FileMapper fileMapper) {
        fileMapper.updateFileStatus(fileId, user.getUserId(), STATUS_ACTIVE);
    }

    /**
     * 更新文件夹及其子项的status
     * @param folderId
     * @param user
     * @param status
     * @param fileMapper
     * @param folderMapper
     */
    public static void handleUpdateFolderStatus(Integer folderId, User user, String status, FileMapper fileMapper, FolderMapper folderMapper) {
        if (status.equalsIgnoreCase(STATUS_ACTIVE)) {
            softRecoverFolderRecursively(folderId, user.getUserId(), fileMapper, folderMapper);
        } else if (status.equalsIgnoreCase(STATUS_DELETED)) {
            softDeleteFolderRecursively(folderId, user.getUserId(), fileMapper, folderMapper);
        }
    }

    /**
     * 递归设置子文件夹和文件的状态为 'deleted'
     * @param folderId
     * @param userId
     * @param fileMapper
     * @param folderMapper
     */
    private static void softDeleteFolderRecursively(Integer folderId, Integer userId, FileMapper fileMapper, FolderMapper folderMapper) {
        recursiveUpdateFolderStatus(folderId, userId, STATUS_DELETED, fileMapper, folderMapper);
    }

    /**
     * 递归设置子文件夹和文件的状态为 'active'
     * @param folderId
     * @param userId
     * @param fileMapper
     * @param folderMapper
     */
    private static void softRecoverFolderRecursively(Integer folderId, Integer userId, FileMapper fileMapper, FolderMapper folderMapper) {
        recursiveUpdateFolderStatus(folderId, userId, STATUS_ACTIVE, fileMapper, folderMapper);
    }

    /**
     * 递归更新文件夹folder的状态status
     * @param folderId
     * @param userId
     * @param status
     * @param fileMapper
     * @param folderMapper
     */
    private static void recursiveUpdateFolderStatus(Integer folderId, Integer userId, String status, FileMapper fileMapper, FolderMapper folderMapper) {
        // 根据folderId查找一层子目录的id
        List<Integer> subFoldersId = folderMapper.findSubFoldersId(folderId, userId);

        // 递归并更新所有子文件夹的文件状态status
        for (Integer subFolderId : subFoldersId) {
            recursiveUpdateFolderStatus(subFolderId, userId, status, fileMapper, folderMapper);
        }

        // 查找并标记当前文件夹中的文件的status
        setFileInFolderStatus(folderId, userId, status, fileMapper);

        // 标记当前文件夹status为deleted
        folderMapper.updateFolderStatus(folderId, userId, status);

    }

    /**
     * 设置在某个文件夹中的所有文件状态, 'active' 或 'deleted'
     * @param parentFolderId
     * @param userId
     * @param status
     * @param fileMapper
     */
    private static void setFileInFolderStatus(Integer parentFolderId, Integer userId, String status, FileMapper fileMapper) {
        List<Integer> filesId = fileMapper.findFilesIdInFolder(parentFolderId, userId);
        for(Integer fileId: filesId) {
            fileMapper.updateFileStatus(fileId, userId, status);
        }
    }



    /**
     * 实现创建文件夹功能, 同级目录下不能有同名文件夹
     * @param folderName
     * @param user
     * @param folderMapper
     * @return
     */
    public static ResponseEntity<String> handleCreateFolder(String folderName, Integer parentFolderId,  User user, FolderMapper folderMapper) {
        try {
            List<Folder> foldersList;
            if (parentFolderId == null) {
                // 如果parentFolderName值为null,意味着当前父目录为根目录
                foldersList = folderMapper.getRootFolder(user.getUserId());
            } else {
                // 否则根据当前folderId查找该目录下的所有文件夹
                foldersList = folderMapper.getFoldersByParentFolderIdAndUserId(parentFolderId, user.getUserId());
            }

            // 判断当前目录下是否有同名文件夹,如果有同名文件夹,创建失败
            for (Folder folder: foldersList) {
                if (folder.getFolderName().equals(folderName)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件夹名不能重复");
                }
            }

            Folder folder = new Folder(folderName, parentFolderId, user.getUserId(), getTime());
            folderMapper.insertFolder(folder);
            // 创建成功，返回201 Created状态码和一些可选的响应体信息
            return ResponseEntity.status(HttpStatus.CREATED).body("文件夹创建成功");
        } catch (Exception e) {
            // 发生错误，返回服务器内部错误状态码500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件夹创建失败: " + e.getMessage());
        }
    }

    /**
     * 通过文件id,重命名文件
      * @param itemId
     * @param newItemName
     * @param itemType
     * @param user
     * @param fileMapper
     * @return
     */
    public static ResponseEntity<String> handleRenameItem(Integer itemId,
                                                          String newItemName,
                                                          String itemType,
                                                          User user,
                                                          FileMapper fileMapper,
                                                          FolderMapper folderMapper) {
        int result = 0;
        if (itemType.equals("file")) {
            result = fileMapper.updateFileNameById(itemId, newItemName, user.getUserId());
        } else if (itemType.equals("folder")) {
            result = folderMapper.updateFolderNameById(itemId, newItemName, user.getUserId());
        }

        if (result != 0) {
            return ResponseEntity.ok().body("handle rename item success");
        } else {
            return ResponseEntity.internalServerError().body("item type error");
        }
    }

    /**
     * 处理移动项目
     * @param itemType
     * @param targetFolderId
     * @param user
     * @param fileMapper
     * @param folderMapper
     * @return
     */
    public static ResponseEntity<String> handleMoveItem(Integer itemId,
                                                        String itemType,
                                                        Integer targetFolderId,
                                                        User user,
                                                        FileMapper fileMapper,
                                                        FolderMapper folderMapper) {
        int result = 0;
        if (itemType.equals("file")) {
            result = fileMapper.updateParentFolderId(itemId, targetFolderId, user.getUserId());
        } else if (itemType.equals("folder")) {
            result = folderMapper.updateParentFolderId(itemId, targetFolderId, user.getUserId());
        }

        if (result != 0) {
            return ResponseEntity.ok().body("handle move item success");
        } else {
            return ResponseEntity.internalServerError().body("item type error");
        }
    }

    /**
     * 将数据通过Base64编码
     * @param data
     * @return
     */
    public static String encodeBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    /**
     * 将数据通过Base64解码
     * @param encodeData
     * @return
     */
    public static String decodeBase64(String encodeData) {
        byte[] decodeBytes = Base64.getDecoder().decode(encodeData);
        return new String(decodeBytes);
    }

    /**
     * 处理文件分享,根据分享链接包含的fileId返回对应的文件
     * @param data
     * @param fileMapper
     * @return
     */
    public static Files handleShare(String data, FileMapper fileMapper) {
        // 解码分享链接获得相关参数
        String decodeData = decodeBase64(data);
        String[] parts = decodeData.split("\\|"); // 格式为"fileId:xxx|userId:xxx"
        Integer fileId = Integer.parseInt(parts[0].split(":")[1]);
        Integer userId = Integer.parseInt(parts[1].split(":")[1]);

        return fileMapper.getFileById(fileId, userId);
    }

    /**
     * 处理用户保存分享的文件
     * @param originalFileId
     * @param originalUserId
     * @param folderId
     * @param user
     * @param fileMapper
     * @param chunkMapper
     * @param fileChunkMapper
     * @return
     */
    public static ResponseEntity<String> handleSaveShareFile(Integer originalFileId,
                                                             Integer originalUserId,
                                                             Integer folderId,
                                                             User user,
                                                             FileMapper fileMapper,
                                                             ChunkMapper chunkMapper,
                                                             FileChunkMapper fileChunkMapper) {
        // 获得原文件的记录
        Files file = fileMapper.getFileById(originalFileId, originalUserId);

        // 生成新的记录
        Files newFile = new Files(file.getFileName(), file.getFileHash(), file.getTotalChunks(), file.getFileSize(), folderId, user.getUserId(), getTime());
        fileMapper.generateShareFileRecord(newFile);

        // 获得原文件的所有文件分片记录,然后插入新记录
        List<FileChunk> fileChunks = fileChunkMapper.getAllFileChunkByFileId(originalFileId);
        for (FileChunk fileChunk: fileChunks) {
            fileChunkMapper.insertFileChunk(new FileChunk(newFile.getFileId(), fileChunk.getChunkId(), fileChunk.getChunkIndex(), getTime()));
            // 更新分片表的分片次数
            chunkMapper.updateReferenceCountById(fileChunk.getChunkId());
        }
        return ResponseEntity.ok().body("newFile id: " + newFile.getFileId());
    }
}
