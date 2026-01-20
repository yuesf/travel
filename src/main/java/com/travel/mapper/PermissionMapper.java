package com.travel.mapper;

import com.travel.entity.Permission;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 权限Mapper接口
 * 
 * @author travel-platform
 */
public interface PermissionMapper {
    
    /**
     * 根据ID查询权限
     */
    Permission selectById(@Param("id") Long id);
    
    /**
     * 根据代码查询权限
     */
    Permission selectByCode(@Param("code") String code);
    
    /**
     * 查询所有权限
     */
    List<Permission> selectAll();
    
    /**
     * 根据父ID查询权限列表
     */
    List<Permission> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 插入权限
     */
    int insert(Permission permission);
    
    /**
     * 更新权限
     */
    int updateById(Permission permission);
    
    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);
}
