package com.travel.service;

import com.travel.entity.Directory;

import java.util.List;

/**
 * 目录服务接口
 * 
 * @author travel-platform
 */
public interface DirectoryService {
    
    /**
     * 获取目录树
     * @return 目录树列表
     */
    List<Directory> getDirectoryTree();
    
    /**
     * 创建目录
     * @param name 目录名称
     * @param parentId 父目录ID（可选，null表示根目录）
     * @return 创建的目录对象
     */
    Directory createDirectory(String name, Long parentId);
    
    /**
     * 根据ID查询目录
     * @param id 目录ID
     * @return 目录对象
     */
    Directory getDirectoryById(Long id);
    
    /**
     * 删除目录
     * @param id 目录ID
     * @return true-删除成功，false-删除失败
     */
    boolean deleteDirectory(Long id);
    
    /**
     * 构建目录路径
     * @param name 目录名称
     * @param parentId 父目录ID
     * @return 目录路径
     */
    String buildDirectoryPath(String name, Long parentId);
}
