package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Chunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChunkMapper {

    // 将分片值插入数据库
    int insertChunk(Chunk chunk);

    // 通过fileHash获得所有的chunk
    List<Chunk> getAllChunksByFileIdAndFileHash(@Param("fileHash") String fileHash);

    // 通过chunkId删除自身记录
    int deleteChunkById(@Param("chunkId") int chunkId);

    // 通过fileHash获得所有所有分片信息,使用聚合函数计算文件总大小
    long getFileSizeByFileId(@Param("fileHash") String fileHash);
}
