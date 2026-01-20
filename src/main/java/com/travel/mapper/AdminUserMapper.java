package com.travel.mapper;

import com.travel.entity.AdminUser;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员Mapper接口
 * 
 * @author travel-platform
 */
public interface AdminUserMapper {
    
    /**
     * 根据ID查询管理员
     */
    AdminUser selectById(@Param("id") Long id);
    
    /**
     * 根据用户名查询管理员
     */
    AdminUser selectByUsername(@Param("username") String username);
    
    /**
     * 根据手机号查询管理员
     */
    AdminUser selectByPhone(@Param("phone") String phone);
    
    /**
     * 插入管理员
     */
    int insert(AdminUser adminUser);
    
    /**
     * 更新管理员信息
     */
    int updateById(AdminUser adminUser);
    
    /**
     * 更新最后登录信息
     */
    int updateLastLoginInfo(@Param("id") Long id, 
                           @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime,
                           @Param("lastLoginIp") String lastLoginIp);
}
