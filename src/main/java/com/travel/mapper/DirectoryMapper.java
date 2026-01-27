package com.travel.mapper;

import com.travel.entity.Directory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 目录Mapper接口
 * 
 * @author travel-platform
 */
@Mapper
public interface DirectoryMapper {
    
    /**
     * 插入目录
     * @param directory 目录对象
     * @return 影响行数
     */
    int insert(Directory directory);
    
    /**
     * 根据ID查询目录
     * @param id 目录ID
     * @return 目录对象
     */
    Directory selectById(@Param("id") Long id);
    
    /**
     * 根据路径查询目录
     * @param path 目录路径
     * @return 目录对象
     */
    Directory selectByPath(@Param("path") String path);
    
    /**
     * 查询所有目录
     * @return 目录列表
     */
    List<Directory> selectAll();
    
    /**
     * 根据父目录ID查询子目录列表
     * @param parentId 父目录ID
     * @return 子目录列表
     */
    List<Directory> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 删除目录
     * @param id 目录ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 检查目录名称是否在同一父目录下已存在
     * @param name 目录名称
     * @param parentId 父目录ID
     * @return 数量
     */
    int countByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);
}
