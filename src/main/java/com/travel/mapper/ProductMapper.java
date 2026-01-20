package com.travel.mapper;

import com.travel.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品Mapper接口
 * 
 * @author travel-platform
 */
public interface ProductMapper {
    
    /**
     * 根据ID查询商品
     */
    Product selectById(@Param("id") Long id);
    
    /**
     * 批量查询商品（根据ID列表）
     * @param ids 商品ID列表
     * @return 商品列表
     */
    List<Product> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 分页查询商品列表
     */
    List<Product> selectList(@Param("name") String name,
                            @Param("categoryId") Long categoryId,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice,
                            @Param("status") Integer status,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);
    
    /**
     * 统计商品总数
     */
    long count(@Param("name") String name,
              @Param("categoryId") Long categoryId,
              @Param("minPrice") BigDecimal minPrice,
              @Param("maxPrice") BigDecimal maxPrice,
              @Param("status") Integer status);
    
    /**
     * 插入商品
     */
    int insert(Product product);
    
    /**
     * 更新商品信息
     */
    int updateById(Product product);
    
    /**
     * 删除商品（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 检查商品是否有关联订单
     */
    long countOrdersByProductId(@Param("productId") Long productId);
    
    /**
     * 更新库存
     */
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);
    
    /**
     * 增加销量
     */
    int increaseSales(@Param("id") Long id, @Param("quantity") Integer quantity);
}
