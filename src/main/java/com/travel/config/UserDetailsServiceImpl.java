package com.travel.config;

import com.travel.entity.AdminUser;
import com.travel.entity.Role;
import com.travel.mapper.AdminUserMapper;
import com.travel.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private RoleService roleService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询管理员
        AdminUser adminUser = adminUserMapper.selectByUsername(username);
        if (adminUser == null) {
            adminUser = adminUserMapper.selectByPhone(username);
        }
        
        if (adminUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 查询角色和权限
        Role role = roleService.getByUserId(adminUser.getId());
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (role != null) {
            // 添加角色
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            
            // 添加权限
            List<String> permissionCodes = roleService.getPermissionCodesByRoleId(role.getId());
            for (String permissionCode : permissionCodes) {
                authorities.add(new SimpleGrantedAuthority(permissionCode));
            }
        } else {
            // 默认角色
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return new org.springframework.security.core.userdetails.User(
            adminUser.getUsername(),
            adminUser.getPassword(),
            adminUser.getStatus() != null && adminUser.getStatus() == 1,
            true,
            true,
            true,
            authorities
        );
    }
}
