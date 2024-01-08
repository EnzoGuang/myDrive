package com.yg.mydrive.entity;

public class Files {
    private int fileId;
    private String fileName;
    private String hashValue;
    private int folderId;
    private int userId;
    private String filePath;
    private String uploadTime;

    @Override
    public String toString() {
        return "Files{" +
                "fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", hashValue='" + hashValue + '\'' +
                ", folderId=" + folderId +
                ", userId=" + userId +
                ", filePath='" + filePath + '\'' +
                ", uploadTime='" + uploadTime + '\'' +
                '}';
    }

    public Files() {}

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

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
