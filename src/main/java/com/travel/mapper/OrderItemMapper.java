package com.travel.mapper;

import com.travel.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单明细Mapper接口
 * 
 * @author travel-platform
 */
public interface OrderItemMapper {
    
    /**
     * 根据订单ID查询订单明细列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 批量查询订单项（根据订单ID列表）
     * @param orderIds 订单ID列表
     * @return 订单项列表
     */
    List<OrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);
    
    /**
     * 批量插入订单明细
     */
    int insertBatch(@Param("items") List<OrderItem> items);
}
