package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Files;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper {

    // 通过用户id和父文件id该查找该路径下所有文件
    List<Files> findFileByUserIdAndFolderId(@Param("userId") int userId, @Param("folderId") Integer folderId);

    // 初始化文件记录,并返回文件id值 ;
    int initializeFile(@Param("file") Files file);

    // 开启文件版本控制
    int versionControlEnable(@Param("fileId") Integer fileId);

    // 获得当前版本id
    int getCurrentVersionId(@Param("fileId") Integer fileId);

    // 更新文件的版本id号
    int updateVersionId(@Param("fileId") Integer fileId, @Param("versionId") Integer versionId);

    // 根据文件id,更新该文件的大小
    Integer updateFileSize(@Param("fileId") Integer fileId, @Param("fileSize") Long fileSize);

    // 查找文件通过文件id
    Files getFileById(@Param("fileId") Integer fileId, @Param("userId") Integer userId);

    // 更新文件名通过文件id
    int updateFileNameById(@Param("fileId") Integer fileId, @Param("newFileName") String newFileName, @Param("userId") Integer userId);

    // 根据目录id移动文件到指定的目录
    int updateParentFolderId(@Param("fileId") Integer fileId, @Param("targetFolderId") Integer targetFolderId, @Param("userId") Integer userId);

    // 生成文件分享保存的记录
    int generateShareFileRecord(@Param("file") Files file);

    // 通过文件夹id查找所有属于该文件夹的文件
    List<Integer> findFilesIdInFolder(@Param("folderId") Integer folderId, @Param("userId") Integer userId);

    // 设置文件的status,status为'active'或'deleted'
    int updateFileStatus(@Param("fileId") Integer fileId, @Param("userId") Integer userId, @Param("status") String status);

    // 获得删除的文件不属于任何已删除的文件夹
    List<Files> findFilesNotInDeletedFolders(@Param("userId") Integer userId);

}
