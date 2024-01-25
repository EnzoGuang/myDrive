package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Chunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChunkMapper {

    // 将分片值插入数据库
    int insertChunk(Chunk chunk);

    // 返回值表示有多少个属于同一个文件的分片的id值被更改,若和分片数相同，则更改成功
    int updateFileIdByFileHash(@Param("fileId") int fileId, @Param("fileHash") String fileHash);

    // 通过fileId和fileHash获得所有的chunk
    List<Chunk> getAllChunksByFileIdAndFileHash(@Param("fileId") int fileId);

    // 通过chunkId删除自身记录
    int deleteChunkById(@Param("chunkId") int chunkId);
}
