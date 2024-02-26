package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.FileVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileVersionMapper {

    // 初始化文件版本控制记录
    int initializeFileVersion(@Param("fileVersion")FileVersion fileVersion);

    // 获得文件版本最大的序号
    Integer getMaxVersionNumber(@Param("fileId") Integer fileId);

    // 已开启版本控制文件创建新的记录
    int createNewFileVersion(@Param("fileVersion") FileVersion fileVersion);

    List<FileVersion> getAllVersionByFileId(@Param("fileId") Integer fileId);
}
