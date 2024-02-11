package com.yg.mydrive.entity;

public class Folder {
    private int folderId;
    private String folderName;
    private Integer parentFolderId;
    private int userId;
    private String createTime;

    public Folder() {}

    public Folder(String folderName, Integer parentFolderId, int userId, String createTime) {
        this.folderName = folderName;
        this.parentFolderId = parentFolderId;
        this.userId = userId;
        this.createTime = createTime;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Integer getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(int parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
