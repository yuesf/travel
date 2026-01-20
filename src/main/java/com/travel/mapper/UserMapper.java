package com.travel.mapper;

import com.travel.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 小程序用户Mapper接口
 * 
 * @author travel-platform
 */
public interface UserMapper {
    
    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);
    
    /**
     * 根据openid查询用户
     */
    User selectByOpenid(@Param("openid") String openid);
    
    /**
     * 根据手机号查询用户
     */
    User selectByPhone(@Param("phone") String phone);
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户信息
     */
    int updateById(User user);
    
    /**
     * 更新首次下单状态
     */
    int updateFirstOrderStatus(@Param("id") Long id, @Param("isFirstOrder") Integer isFirstOrder);
}
