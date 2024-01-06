package com.yg.mydrive.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileService {

    private static final File UPLOAD_DIR = Utils.join(System.getProperty("user.dir"), "uploads");

    public static File getUploadDir() {
        if (!UPLOAD_DIR.exists()) {
            UPLOAD_DIR.mkdir();
        }
        return UPLOAD_DIR;
    }

    /**
     * 处理文件上传
     * @param file
     * @return
     */
    public static ResponseEntity<String> handleUploadFile(MultipartFile file) {
        try {
            // 获得文件的hash值
            String hashValue = getHashOfFile(file);

            String originalName = file.getOriginalFilename();

            // 使用文件的hash值和文件本身名字进行拼接生成具有唯一性的文件名
            String fileName = hashValue.substring(0, 8) + "_" + StringUtils.cleanPath(file.getOriginalFilename());

            // 拼接文件存储的路径
            Path filePath = Utils.join(getUploadDir(), fileName).toPath();

            // 复制文件到目标路径
            FileCopyUtils.copy(file.getInputStream(), Files.newOutputStream(filePath));

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
     * @return
     */
    private static String bytesToHex(byte[] content) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte temp: content) {
            stringBuilder.append(String.format("%02x", temp));
        }
        return stringBuilder.toString();

    }
}
