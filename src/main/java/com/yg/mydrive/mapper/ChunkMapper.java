package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Chunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChunkMapper {

    // 将分片记录插入数据库
    @Options(useGeneratedKeys = true, keyProperty = "chunkId", keyColumn = "id")
    void insertChunk(Chunk chunk);

    // 通过chunkId删除自身记录
    int deleteChunkById(@Param("chunkId") int chunkId);

    // 通过chunkHash查找表里是否保存该分片,如果有返回该分片的id
    Integer checkChunkExistsByHash(@Param("chunkHash") String chunkHash);

    // 如果需要上传的分片在服务端已存储,不需要再存储,只需要增加引用次数
    Integer updateReferenceCountByHash(@Param("chunkId") Integer chunkId, @Param("chunkHash") String chunkHash);

    // 根据分片id值更新分片的引用次数
    Integer updateReferenceCountById(@Param("chunkId") Integer chunkId);

    // 根据id值获得分片的hash值
    String getChunkHashByFileId(@Param("chunkId") Integer chunkId);

    // 根据id值获得分片的大小
    Long getChunkSize(@Param("chunkId") Integer chunkId);
}
