package com.travel.mapper;

import com.travel.entity.ProductCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类Mapper接口
 * 
 * @author travel-platform
 */
public interface ProductCategoryMapper {
    
    /**
     * 根据ID查询分类
     */
    ProductCategory selectById(@Param("id") Long id);
    
    /**
     * 查询所有分类列表
     */
    List<ProductCategory> selectAll(@Param("status") Integer status, @Param("type") String type);
    
    /**
     * 根据父分类ID查询子分类列表
     */
    List<ProductCategory> selectByParentId(@Param("parentId") Long parentId, @Param("status") Integer status, @Param("type") String type);
    
    /**
     * 查询分类树（一级分类及其子分类）
     */
    List<ProductCategory> selectTree(@Param("status") Integer status, @Param("type") String type);
    
    /**
     * 插入分类
     */
    int insert(ProductCategory category);
    
    /**
     * 更新分类信息
     */
    int updateById(ProductCategory category);
    
    /**
     * 删除分类（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 检查分类下是否有商品
     */
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 检查是否有子分类
     */
    long countChildrenByParentId(@Param("parentId") Long parentId);
}
