package com.yg.mydrive.entity;

public class Chunk {
    private int chunkId;
    private String fileHash;
    private int chunkIndex;
    private long chunkSize;
    private String chunkHash;
    private String uploadTime;

    public Chunk(String fileHash, int chunkIndex, long chunkSize, String chunkHash, String uploadTime) {
        this.fileHash = fileHash;
        this.chunkIndex = chunkIndex;
        this.chunkSize = chunkSize;
        this.chunkHash = chunkHash;
        this.uploadTime= uploadTime;
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "chunkId=" + chunkId +
                ", fileId=" + fileHash +
                ", chunkIndex=" + chunkIndex +
                ", chunkSize=" + chunkSize +
                ", chunkHash='" + chunkHash + '\'' +
                ", upload_time='" + uploadTime + '\'' +
                '}';
    }

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
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

    public void setChunkHash(String chunkHash) {
        this.chunkHash = chunkHash;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
