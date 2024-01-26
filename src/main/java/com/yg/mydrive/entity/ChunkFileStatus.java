package com.yg.mydrive.entity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkFileStatus {
    private final String fileId;
    private final int totalChunks;
    private Set<Integer> uploadChunks = ConcurrentHashMap.newKeySet();

    public ChunkFileStatus(String fileId, int totalChunks) {
        this.fileId = fileId;
        this.totalChunks = totalChunks;
    }

    public boolean isUploadComplete() {
        return totalChunks == uploadChunks.size();
    }

    public void markChunkAsUploaded(int chunkIndex) {
        uploadChunks.add(chunkIndex);
    }

    public int getTotalChunks() {
        return totalChunks;
    }
}
