package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Chunk;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChunkMapper {

    int insertChunk(Chunk chunk);
}
