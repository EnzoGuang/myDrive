package com.yg.mydrive.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FolderMapper {
    int createFolder(@Param("folderName") String folderName,
                     @Param("parentFolderId") Integer parentFolderId,
                     @Param("userId") Integer userId,
                     @Param("createTime") String createTime);
}
