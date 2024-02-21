package com.yg.mydrive.entity;

public class Files {
    public int fileId;
    public String fileName;
    public String fileHash;
    public long fileSize;
    public int totalChunks;
    public Integer folderId;
    public Integer userId;
    public Boolean versionControlEnabled;
    public Integer currentVersionId;
    public String uploadTime;
    public String status;
    public String deleteTime;

    public Files(String fileName, String fileHash, int totalChunks, Integer folderId, Integer userId, String uploadTime) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.totalChunks = totalChunks;
        this.folderId = folderId;
        this.userId = userId;
        this.uploadTime = uploadTime;
    }

    public Files(String fileName, String fileHash, int totalChunks, long fileSize, Integer folderId, Integer userId, String uploadTime) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.totalChunks = totalChunks;
        this.fileSize = fileSize;
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

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Boolean getVersionControlEnabled() {
        return versionControlEnabled;
    }

    public void setVersionControlEnabled(Boolean versionControlEnabled) {
        this.versionControlEnabled = versionControlEnabled;
    }

    public Integer getCurrentVersionId() {
        return currentVersionId;
    }

    public void setCurrentVersionId(Integer currentVersionId) {
        this.currentVersionId = currentVersionId;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(String deleteTime) {
        this.deleteTime = deleteTime;
    }
}
