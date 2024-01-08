package com.yg.mydrive.service;

import com.yg.mydrive.entity.Files;
import com.yg.mydrive.entity.User;
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
import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileService {

    private static final File UPLOAD_DIR = Utils.join(System.getProperty("user.dir"), "uploads");

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
            files.setHashValue(hashValue);
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

    public static ResponseEntity<Resource> handleDownloadFile(String fileName, User user, FileMapper fileMapper) throws MalformedURLException {
        // 获得文件的hash值
        String fileHash = fileMapper.getHashOfFileByUserIdAndFileName(user.getUserId(), fileName);
        // 获得文件存储在文件系统的实际存储名字
        String fileStorageName = concatHashPrefixAndFileName(fileHash, fileName);
        // 获得文件的实际存储路径
        Path fileStoragePath = getUploadDir().toPath().resolve(fileStorageName);

        UrlResource resource = new UrlResource(fileStoragePath.toUri());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok().headers(headers).body(resource);
    }


    /**
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
}
