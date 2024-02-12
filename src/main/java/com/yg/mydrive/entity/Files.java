package com.yg.mydrive.entity;

public class Files {
    private int fileId;
    private String fileName;
    private String fileHash;
    private long fileSize;
    private int totalChunks;
    private Integer folderId;
    private Integer userId;
    private String uploadTime;

    public Files(String fileName, String fileHash, int totalChunks, Integer folderId, Integer userId, String uploadTime) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.totalChunks = totalChunks;
        this.folderId = folderId;
        this.userId = userId;
        this.uploadTime = uploadTime;
    }

    public Files(String fileName, String fileHash, int totalChunks, Integer userId, String uploadTime) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.totalChunks = totalChunks;
        this.userId = userId;
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return "Files{" +
                "fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", fileHash='" + fileHash + '\'' +
                ", fileSize=" + fileSize +
                ", totalChunks=" + totalChunks +
                ", folderId=" + folderId +
                ", userId=" + userId +
                ", uploadTime='" + uploadTime + '\'' +
                '}';
    }

    public Files() {}

    public Files(String fileHash) {
        this.fileHash = fileHash;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
