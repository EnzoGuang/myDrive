package com.yg.mydrive.entity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkFileStatus {
    private final Integer fileId;
    private final int totalChunks;
    private Set<Integer> uploadChunks = ConcurrentHashMap.newKeySet();
    private long totalSize;

    // 临时存储分片信息，所有分片的id,key存储index分片序号,value存储该分片的id
    private Map<Integer, Integer> totalChunksId = new ConcurrentHashMap<>();

    public Map<Integer, Integer> getTotalChunksId() {
        return totalChunksId;
    }

    public ChunkFileStatus(Integer fileId, int totalChunks) {
        this.fileId = fileId;
        this.totalChunks = totalChunks;
    }

    public boolean isUploadComplete() {
        return totalChunks == uploadChunks.size();
    }

    // 标记当前上传的分片
    public void markChunkAsUploaded(int chunkIndex, int chunkId, long chunkSize) {
        uploadChunks.add(chunkIndex);
        totalChunksId.put(chunkIndex, chunkId);
        totalSize += chunkSize;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public Integer getFileId() {
        return fileId;
    }

    public Set<Integer> getUploadChunks() {
        return uploadChunks;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
