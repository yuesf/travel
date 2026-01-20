package com.travel.mapper;

import com.travel.entity.UserAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收货地址Mapper接口
 * 
 * @author travel-platform
 */
public interface UserAddressMapper {
    
    /**
     * 根据ID查询收货地址
     */
    UserAddress selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询收货地址列表（默认地址排在第一位）
     */
    List<UserAddress> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和地址ID查询收货地址（验证地址是否属于该用户）
     */
    UserAddress selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 插入收货地址
     */
    int insert(UserAddress userAddress);
    
    /**
     * 更新收货地址
     */
    int updateById(UserAddress userAddress);
    
    /**
     * 删除收货地址
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 取消用户所有地址的默认状态
     */
    int cancelAllDefault(@Param("userId") Long userId);
    
    /**
     * 设置指定地址为默认地址
     */
    int setDefault(@Param("id") Long id, @Param("userId") Long userId);
}
