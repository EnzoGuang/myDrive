package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.FileChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileChunkMapper {
    // 将文件分片关联关系插入表中
    @Options(useGeneratedKeys = true, keyProperty = "fileChunk.fileChunkId", keyColumn = "id")
    int insertFileChunk(@Param("fileChunk") FileChunk fileChunk);
}
