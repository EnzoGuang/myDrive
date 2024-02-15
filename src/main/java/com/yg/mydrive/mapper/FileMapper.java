package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Files;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper {

    // 通过用户id和父文件id该查找该路径下所有文件
    List<Files> findFileByUserIdAndFolderId(@Param("userId") int userId, @Param("folderId") Integer folderId);

    // 初始化文件记录,并返回文件id值 ;
    int initializeFile(@Param("file") Files file);

    // 根据文件id,更新该文件的大小
    Integer updateFileSize(@Param("fileId") Integer fileId, @Param("fileSize") Long fileSize);
}
