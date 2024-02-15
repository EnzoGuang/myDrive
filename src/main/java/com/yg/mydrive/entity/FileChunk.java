package com.yg.mydrive.entity;

public class FileChunk {
    private Integer fileChunkId;
    private Integer fileId;
    private Integer chunkId;
    private Integer chunkIndex;
    private Integer versionId;
    private String uploadTime;

    public FileChunk() {}

    public FileChunk(Integer fileId, Integer chunkId, Integer chunkIndex, String uploadTime) {
        this.fileId = fileId;
        this.chunkId = chunkId;
        this.chunkIndex = chunkIndex;
        this.uploadTime = uploadTime;
    }

    public Integer getFileChunkId() {
        return fileChunkId;
    }

    public void setFileChunkId(Integer fileChunkId) {
        this.fileChunkId = fileChunkId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getChunkId() {
        return chunkId;
    }

    public void setChunkId(Integer chunkId) {
        this.chunkId = chunkId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
