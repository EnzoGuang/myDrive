package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.FileVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileVersionMapper {

    // 初始化文件版本控制记录
    int initializeFileVersion(@Param("fileVersion")FileVersion fileVersion);
}
