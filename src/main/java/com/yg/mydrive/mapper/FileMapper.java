package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Files;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface FileMapper {
    int insertFile(Files file);

    List<Files> findFileByUserIdAndFolderId(@Param("userId") int userId, @Param("folderId") Integer folderId);
}
