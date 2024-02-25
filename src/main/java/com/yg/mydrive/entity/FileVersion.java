package com.yg.mydrive.entity;

public class FileVersion {
    private Integer fileVersionId;
    private Integer fileId;
    private Integer versionNumber;
    private String message;
    private String uploadTime;

    public FileVersion() {}

    public FileVersion(Integer fileVersionId, Integer fileId, Integer versionNumber, String message, String uploadTime) {
        this.fileVersionId = fileVersionId;
        this.fileId = fileId;
        this.versionNumber = versionNumber;
        this.message = message;
        this.uploadTime = uploadTime;
    }

    public FileVersion(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getFileVersionId() {
        return fileVersionId;
    }

    public void setFileVersionId(Integer fileVersionId) {
        this.fileVersionId = fileVersionId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
