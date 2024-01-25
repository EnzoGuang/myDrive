package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Files;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper {

    // 将文件加入表中
    int insertFile(Files file);

    int findIdByFileHash(@Param("fileHash") String fileHash);

    List<Files> findFileByUserIdAndFolderId(@Param("userId") int userId, @Param("folderId") Integer folderId);

    String getHashOfFileByUserIdAndFileName(@Param("userId") Integer userId, @Param("fileName") String fileName);

    int deleteFileByFileName(@Param("userId") Integer userId, @Param("fileName") String fileName);

    // 通过文件名和用户id查找文件的hash值
    String getFileHashByFileNameAndUserId(@Param("fileName") String fileName, @Param("userId") Integer userId);

    // 通过文件名和用户id获得文件的id值
    Integer getFileIdByFileNameAndUserId(@Param("fileName") String fileName, @Param("userId") Integer userId);

    // 通过文件名和用户id删除该文件记录
    int deleteFileByFileNameAndUserId(@Param("fileName") String fileName, @Param("userId") Integer userId);
}
