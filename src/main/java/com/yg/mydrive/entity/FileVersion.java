package com.yg.mydrive.entity;

public class FileVersion {
    private Integer fileVersionId;
    private String fileId;
    private Integer versionNumber;
    private String message;
    private String uploadTime;

    public FileVersion() {}

    public FileVersion(Integer fileVersionId, String fileId, Integer versionNumber, String message, String uploadTime) {
        this.fileVersionId = fileVersionId;
        this.fileId = fileId;
        this.versionNumber = versionNumber;
        this.message = message;
        this.uploadTime = uploadTime;
    }

    public Integer getFileVersionId() {
        return fileVersionId;
    }

    public void setFileVersionId(Integer fileVersionId) {
        this.fileVersionId = fileVersionId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
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
