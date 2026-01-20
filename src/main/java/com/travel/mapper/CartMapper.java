package com.travel.mapper;

import com.travel.entity.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车Mapper接口
 * 
 * @author travel-platform
 */
public interface CartMapper {
    
    /**
     * 根据ID查询购物车
     */
    Cart selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询购物车列表
     */
    List<Cart> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和商品信息查询购物车
     */
    Cart selectByUserAndItem(@Param("userId") Long userId,
                             @Param("itemType") String itemType,
                             @Param("itemId") Long itemId);
    
    /**
     * 插入购物车
     */
    int insert(Cart cart);
    
    /**
     * 更新购物车
     */
    int updateById(Cart cart);
    
    /**
     * 删除购物车
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据用户ID删除所有购物车
     */
    int deleteByUserId(@Param("userId") Long userId);
}
