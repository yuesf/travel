package com.travel.mapper;

import com.travel.entity.Coupon;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券Mapper接口
 * 
 * @author travel-platform
 */
public interface CouponMapper {
    
    /**
     * 根据ID查询优惠券
     */
    Coupon selectById(@Param("id") Long id);
}
