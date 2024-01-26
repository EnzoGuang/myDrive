package com.yg.mydrive.service;

import com.yg.mydrive.entity.Chunk;
import com.yg.mydrive.entity.ChunkFileStatus;
import com.yg.mydrive.entity.Files;
import com.yg.mydrive.entity.User;
import com.yg.mydrive.mapper.ChunkMapper;
import com.yg.mydrive.mapper.FileMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileService {

    private static final File UPLOAD_DIR = Utils.join(System.getProperty("user.dir"), "uploads");
    private static Map<String, ChunkFileStatus> statusMap = new ConcurrentHashMap<>();

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
     * @deprecated 使用 {@link #handleUploadChunkFile}替代,新方法使用分片上传
     * 处理文件上传,通过session获取当前用户
     * @param file
     * @param session
     * @param fileMapper
     * @return
     */
    public static ResponseEntity<String> handleUploadFile(MultipartFile file,
                                                          HttpSession session,
                                                          FileMapper fileMapper) {
        try {
            // 通过session获得当前用户
            User user = (User) session.getAttribute("currentUser");
            // 获得文件的hash值
            String hashValue = getHashOfFile(file);

            String originalName = file.getOriginalFilename();

            // 使用文件的hash值和文件本身名字进行拼接生成具有唯一性的文件名
            String fileName = hashValue.substring(0, 8) + "_" + StringUtils.cleanPath(file.getOriginalFilename());

            // 拼接文件存储的路径
            Path filePath = Utils.join(getUploadDir(), fileName).toPath();

            // 复制文件到目标路径
            FileCopyUtils.copy(file.getInputStream(), java.nio.file.Files.newOutputStream(filePath));
            Files files = new Files();
            files.setFileName(originalName);
            files.setFileHash(hashValue);
            files.setUserId(user.getUserId());
            files.setUploadTime(getTime());
            files.setFolderId(0);
            fileMapper.insertFile(files);


            return new ResponseEntity<>("文件上传成功" + originalName, HttpStatus.OK);

        } catch (IOException e) {
            return new ResponseEntity<>("文件上传失败" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理分片文件上传
     * @param chunk
     * @param index
     * @param clientChunkHash
     * @param totalChunks
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static ResponseEntity<String> handleUploadChunkFile(int userId,
                                                               MultipartFile chunk,
                                                               String fileName,
                                                               int index,
                                                               String fileHash,
                                                               String clientChunkHash,
                                                               int totalChunks,
                                                               ChunkMapper chunkMapper,
                                                               FileMapper fileMapper) throws NoSuchAlgorithmException, IOException {
        try {
            // 判断文件在传输过程中是否被篡改
            String chunkHash = getHashOfFile(chunk);
            if (!chunkHash.equals(clientChunkHash)) {
                return ResponseEntity.badRequest().body("Chunk hash mismatch");
            }
            ChunkFileStatus chunkFileStatus = statusMap.computeIfAbsent(fileHash, k -> new ChunkFileStatus(fileHash, totalChunks));

            // 保存分片到文件系统中
            saveChunkToUploadDir(chunk, fileHash, index, chunkHash, chunkMapper);

            // 标记该分片上传成功
            chunkFileStatus.markChunkAsUploaded(index);

            // 判断文件的所有分片是否都上传完毕
            if (chunkFileStatus.isUploadComplete()) {
                fileMapper.insertFile(new Files(fileName, fileHash, chunkFileStatus.getTotalChunks(), userId, getTime()));

                // 通过文件的hash值获得在表中的id
                int fileId = fileMapper.findIdByFileHash(fileHash);

                // 通过fileHash获得分片大小,累加获得文件大小,写入文件表
                long fileSize = chunkMapper.getFileSizeByFileId(fileHash);

                // 将文件大小插入文件表
                fileMapper.updateFileSizeByFileId(fileId, fileSize);

                statusMap.remove(fileHash);
                return ResponseEntity.ok().body("File uploaded successful");
            }

            return ResponseEntity.ok().body("Chunk " + index + " uploaded successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload chunk " + index + ": " + e.getMessage());
        }
    }

    /**
     * 存储分片到文件系统中
     * @param chunk
     * @param fileHash
     * @param index
     * @param chunkHash
     * @param chunkMapper
     * @throws IOException
     */
    private static void saveChunkToUploadDir(MultipartFile chunk, String fileHash, int index, String chunkHash, ChunkMapper chunkMapper) throws IOException {
        // 通过分片的hash值获得存储路径
        File chunkStoragePath = getDirPathOfChunkToSave(chunkHash);

        // 将分片进行存储
        chunk.transferTo(chunkStoragePath);

        // 将该分片记录插入数据库
        chunkMapper.insertChunk(new Chunk(fileHash, index, chunk.getSize(), chunkHash, getTime()));
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
     * 获得分片的存储路径,用于删除分片
     * @param chunkHash
     * @return
     */
    private static File getDirPathOfChunkToDelete(String chunkHash) {
        File chunkDir = Utils.join(getUploadDir(), chunkHash.substring(0, 2));
        return Utils.join(chunkDir, chunkHash.substring(2));
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


    // TODO 分片上传后存储路径该变，函数逻辑改变
    public static ResponseEntity<Resource> handleDownloadFile(String fileName, User user, FileMapper fileMapper) throws MalformedURLException {
        Map<String, String> map = getFileHashAndStorageName(fileName, user, fileMapper);
        // 获得文件的hash值
        String fileHash = map.get("fileHash");
        // 获得文件存储在文件系统的实际存储名字
        String fileStorageName = map.get("fileStorageName");
        // 获得文件的实际存储路径
        Path fileStoragePath = getUploadDir().toPath().resolve(fileStorageName);

        UrlResource resource = new UrlResource(fileStoragePath.toUri());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    /** TODO 需要更改
     * helper method 返回一个map,里面包含文件hash值和文件实际存储名字
     * 只存放两个键值对
     *  1.key:fileHash
     *  2.key:fileStorageName
     */
    private static Map<String, String> getFileHashAndStorageName(String fileName, User user, FileMapper fileMapper) {
        HashMap<String, String> map = new HashMap<>();
        String fileHash = fileMapper.getHashOfFileByUserIdAndFileName(user.getUserId(), fileName);
        String fileStorageName = concatHashPrefixAndFileName(fileHash, fileName);
        map.put("fileHash", fileHash);
        map.put("fileStorageName", concatHashPrefixAndFileName(fileHash, fileName));
        return map;
    }

    /** TODO 需要更改
     * 实际文件名存储在文件系统是通过 '前八位hash值' + '_' + '文件名' 组成
     */
    public static String concatHashPrefixAndFileName(String hash, String filename) {
        try {
            if (hash.length() < 8) {
                throw new Exception("该hash值长度小于8, 不符合拼接文件名");
            } else {
                return hash.substring(0, 8) + "_" + filename;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * 通过文件名和用户id获得该文件的hash值
     * @param fileName
     * @param user
     * @param fileMapper
     * @return
     */
    private static String getFileHash(String fileName, User user, FileMapper fileMapper) {
        return fileMapper.getFileHashByFileNameAndUserId(fileName, user.getUserId());
    }

    /**
     * 通过文件名和用户id获得该文件的hash值
     * @param fileName
     * @param user
     * @param fileMapper
     * @return
     */
    private static Integer getFileId(String fileName, User user, FileMapper fileMapper) {
        return fileMapper.getFileIdByFileNameAndUserId(fileName, user.getUserId());
    }

    /**
     * 通过文件名删除文件,包含删除文件记录和分片记录,并在文件系统中删除文件
      * @param fileName
     * @param user
     * @param fileMapper
     * @param chunkMapper
     * @return
     */
    public static int handleDeleteFileByName(String fileName, User user, FileMapper fileMapper, ChunkMapper chunkMapper) {
        String fileHash = getFileHash(fileName, user, fileMapper);
        Integer fileId = getFileId(fileName, user, fileMapper);

        // 通过fileId和fileHash获得该文件的所有分片chunks
        List<Chunk> chunks = chunkMapper.getAllChunksByFileIdAndFileHash(fileHash);

        // 用于记录当前被已经被删除chunk个数
        int deletedChunks = 0;

        for (Chunk chunk: chunks) {
            File chunkPathToDelete = getDirPathOfChunkToDelete(chunk.getChunkHash());
            if (chunkPathToDelete.exists()) {
                // 在文件系统中删除该分片
                chunkPathToDelete.delete();

                // 如果该分片所处目录为空的话,删除该目录
                File parentDir = new File(chunkPathToDelete.getParent());
                if (parentDir.isDirectory()) {
                    File[] files = parentDir.listFiles();
                    if (files != null && files.length == 0) {
                        parentDir.delete();
                    }
                }

                // 删除分片表中的记录
                int result = chunkMapper.deleteChunkById(chunk.getChunkId());
                if (result == 1) {
                    deletedChunks++;
                }
            }
        }
        if (deletedChunks == chunks.size()) {
            return fileMapper.deleteFileByFileNameAndUserId(fileName, user.getUserId());
        }
        return 0;
    }
}
