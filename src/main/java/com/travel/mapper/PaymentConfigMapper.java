package com.travel.mapper;

import com.travel.entity.PaymentConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付配置Mapper接口
 * 
 * @author travel-platform
 */
@Mapper
public interface PaymentConfigMapper {
    
    /**
     * 根据ID查询
     */
    PaymentConfig selectById(Long id);
    
    /**
     * 根据AppID查询
     */
    PaymentConfig selectByAppId(@Param("appId") String appId);
    
    /**
     * 查询启用的配置
     */
    PaymentConfig selectEnabled();
    
    /**
     * 插入
     */
    int insert(PaymentConfig config);
    
    /**
     * 更新
     */
    int updateById(PaymentConfig config);
    
    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
