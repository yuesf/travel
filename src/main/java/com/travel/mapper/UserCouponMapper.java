package com.travel.mapper;

import com.travel.entity.UserCoupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户优惠券Mapper接口
 * 
 * @author travel-platform
 */
public interface UserCouponMapper {
    
    /**
     * 查询用户优惠券列表
     */
    List<UserCoupon> selectByUserId(
        @Param("userId") Long userId,
        @Param("status") Integer status,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );
    
    /**
     * 统计用户优惠券数量
     */
    long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 根据ID查询用户优惠券
     */
    UserCoupon selectById(@Param("id") Long id);
    
    /**
     * 插入用户优惠券
     */
    int insert(UserCoupon userCoupon);
    
    /**
     * 更新用户优惠券
     */
    int updateById(UserCoupon userCoupon);
    
    /**
     * 更新用户优惠券状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
