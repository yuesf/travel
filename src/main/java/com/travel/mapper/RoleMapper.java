package com.travel.mapper;

import com.travel.entity.Role;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 角色Mapper接口
 * 
 * @author travel-platform
 */
public interface RoleMapper {
    
    /**
     * 根据ID查询角色
     */
    Role selectById(@Param("id") Long id);
    
    /**
     * 根据代码查询角色
     */
    Role selectByCode(@Param("code") String code);
    
    /**
     * 查询所有角色
     */
    List<Role> selectAll();
    
    /**
     * 插入角色
     */
    int insert(Role role);
    
    /**
     * 更新角色
     */
    int updateById(Role role);
    
    /**
     * 根据用户ID查询角色
     */
    Role selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询角色的权限列表
     */
    List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);
}
