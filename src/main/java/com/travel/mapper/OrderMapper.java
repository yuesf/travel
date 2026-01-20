package com.travel.mapper;

import com.travel.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单Mapper接口
 * 
 * @author travel-platform
 */
public interface OrderMapper {
    
    /**
     * 根据ID查询订单
     */
    Order selectById(@Param("id") Long id);
    
    /**
     * 根据订单号查询订单
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 查询用户订单列表
     */
    List<Order> selectByUserId(
        @Param("userId") Long userId,
        @Param("status") String status,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );
    
    /**
     * 统计用户订单数量
     */
    long countByUserId(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * 插入订单
     */
    int insert(Order order);
    
    /**
     * 更新订单
     */
    int updateById(Order order);
    
    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
