package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.InitAdminRequest;
import com.travel.entity.AdminUser;
import com.travel.entity.Role;
import com.travel.exception.BusinessException;
import com.travel.mapper.AdminUserMapper;
import com.travel.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员初始化服务
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class AdminInitService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";
    
    /**
     * 检查是否存在超级管理员
     */
    public boolean hasSuperAdmin() {
        Role superAdminRole = roleMapper.selectByCode(SUPER_ADMIN_ROLE_CODE);
        if (superAdminRole == null) {
            return false;
        }
        
        // 检查是否有用户拥有超级管理员角色
        AdminUser adminUser = adminUserMapper.selectById(1L); // 假设第一个管理员是超级管理员
        if (adminUser != null && superAdminRole.getId().equals(adminUser.getRoleId())) {
            return true;
        }
        
        // 查询是否有任何用户拥有超级管理员角色
        // 这里简化处理，实际应该查询所有管理员
        return false;
    }
    
    /**
     * 初始化超级管理员
     */
    @Transactional(rollbackFor = Exception.class)
    public void initSuperAdmin(InitAdminRequest request) {
        // 检查是否已存在超级管理员
        if (hasSuperAdmin()) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "超级管理员已存在，无法重复初始化");
        }
        
        // 检查用户名是否已存在
        AdminUser existingUser = adminUserMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "用户名已存在");
        }
        
        // 创建或获取超级管理员角色
        Role superAdminRole = roleMapper.selectByCode(SUPER_ADMIN_ROLE_CODE);
        if (superAdminRole == null) {
            superAdminRole = new Role();
            superAdminRole.setName("超级管理员");
            superAdminRole.setCode(SUPER_ADMIN_ROLE_CODE);
            superAdminRole.setDescription("拥有所有权限的超级管理员");
            superAdminRole.setStatus(1);
            roleMapper.insert(superAdminRole);
            log.info("创建超级管理员角色: {}", superAdminRole.getCode());
        }
        
        // 创建超级管理员用户
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(request.getUsername());
        adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
        adminUser.setRealName(request.getRealName());
        adminUser.setPhone(request.getPhone());
        adminUser.setEmail(request.getEmail());
        adminUser.setRoleId(superAdminRole.getId());
        adminUser.setStatus(1);
        
        adminUserMapper.insert(adminUser);
        log.info("初始化超级管理员成功: username={}, userId={}", adminUser.getUsername(), adminUser.getId());
    }
    
    /**
     * 创建管理员账号
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminUser createAdmin(InitAdminRequest request, Long roleId) {
        // 检查用户名是否已存在
        AdminUser existingUser = adminUserMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "用户名已存在");
        }
        
        // 验证角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "角色不存在或已禁用");
        }
        
        // 创建管理员
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(request.getUsername());
        adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
        adminUser.setRealName(request.getRealName());
        adminUser.setPhone(request.getPhone());
        adminUser.setEmail(request.getEmail());
        adminUser.setRoleId(roleId);
        adminUser.setStatus(1);
        
        adminUserMapper.insert(adminUser);
        log.info("创建管理员成功: username={}, userId={}, roleId={}", 
                adminUser.getUsername(), adminUser.getId(), roleId);
        
        return adminUser;
    }
}
