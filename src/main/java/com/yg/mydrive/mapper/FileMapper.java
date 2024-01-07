package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Files;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface FileMapper {
    int insertFile(Files file);

    Files findFileByUserIdAndFolderId(@Param("userId") int userId, @Param("folderId") Integer folderId);
}
