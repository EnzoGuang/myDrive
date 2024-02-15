package com.yg.mydrive.entity;

public class Chunk {
    private int chunkId;
    private String chunkHash;
    private Integer referenceCount;
    private long chunkSize;
    private String uploadTime;

    public Chunk() {}

    public Chunk(String chunkHash, long chunkSize, String uploadTime) {
        this.chunkHash = chunkHash;
        this.chunkSize = chunkSize;
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "chunkId=" + chunkId +
                ", chunkHash='" + chunkHash + '\'' +
                ", referenceCount=" + referenceCount +
                ", chunkSize=" + chunkSize +
                ", uploadTime='" + uploadTime + '\'' +
                '}';
    }

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getChunkHash() {
        return chunkHash;
    }

    public Integer getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(Integer referenceCount) {
        this.referenceCount = referenceCount;
    }

    public void setChunkHash(String chunkHash) {
        this.chunkHash = chunkHash;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }
}
