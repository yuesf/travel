package com.travel.service;

import com.travel.entity.Permission;
import com.travel.entity.Role;
import com.travel.mapper.PermissionMapper;
import com.travel.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class RoleService {
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    /**
     * 根据ID查询角色
     */
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }
    
    /**
     * 根据代码查询角色
     */
    public Role getByCode(String code) {
        return roleMapper.selectByCode(code);
    }
    
    /**
     * 查询所有角色
     */
    public List<Role> getAll() {
        return roleMapper.selectAll();
    }
    
    /**
     * 根据用户ID查询角色
     */
    public Role getByUserId(Long userId) {
        return roleMapper.selectByUserId(userId);
    }
    
    /**
     * 根据角色ID查询权限代码列表
     */
    public List<String> getPermissionCodesByRoleId(Long roleId) {
        return roleMapper.selectPermissionCodesByRoleId(roleId);
    }
    
    /**
     * 根据角色ID查询权限列表
     */
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return permissionMapper.selectByRoleId(roleId);
    }
    
    /**
     * 查询所有权限
     */
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectAll();
    }
}
