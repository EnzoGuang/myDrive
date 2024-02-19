package com.yg.mydrive.mapper;

import com.yg.mydrive.entity.Folder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FolderMapper {
    int insertFolder(Folder folder);

    // 查看用户根目录下的所有目录
    List<Folder> getRootFolder(@Param("userId") Integer userId);

    // 根据用户当前目录id查询该目录下的所有子目录
    List<Folder> getFoldersByParentFolderIdAndUserId(@Param("parentFolderId") Integer parentFolderId, @Param("userId") Integer userId);

    // 根据目录名查询目录id
    Integer getFolderIdByFolderName(@Param("folderName") String folderName, @Param("userId") Integer userId);

    // 查看当前用户的所有目录
    List<Folder> getAllFoldersByUserId(@Param("userId") Integer userId);

    // 根据目录id移动文件夹到指定的目录
    int updateParentFolderId(@Param("folderId") Integer folderId, @Param("targetFolderId") Integer targetFolderId, @Param("userId") Integer userId);

    // 更新目录名
    int updateFolderNameById(@Param("folderId") Integer folderId, @Param("targetFolderName") String targetFolderName, @Param("userId") Integer userId);

}
