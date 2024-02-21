package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.FileChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileChunkMapper {
    // 将文件分片关联关系插入表中
    @Options(useGeneratedKeys = true, keyProperty = "fileChunk.fileChunkId", keyColumn = "id")
    int insertFileChunk(@Param("fileChunk") FileChunk fileChunk);

    // 通过文件id和版本id获得所有分片的id
    List<Integer> getAllChunksId(@Param("fileId") Integer fileId, @Param("versionId") Integer versionId);

    // 通过fileId获得所有的FileChunk记录
    List<FileChunk> getAllFileChunkByFileId(@Param("fileId") Integer fileId);

}
