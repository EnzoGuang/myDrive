package com.yg.mydrive.entity;

public class Files {
    private int fileId;
    private String fileName;
    private String fileHash;
    private int folderId;
    private int userId;
    private String uploadTime;

    public Files(String fileName, String fileHash, int userId, String uploadTime) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.userId = userId;
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return "Files{" +
                "fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", hashValue='" + fileHash + '\'' +
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


    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
