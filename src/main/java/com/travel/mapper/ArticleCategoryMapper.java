package com.travel.mapper;

import com.travel.entity.ArticleCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章分类Mapper接口
 * 
 * @author travel-platform
 */
public interface ArticleCategoryMapper {
    
    /**
     * 根据ID查询分类
     */
    ArticleCategory selectById(@Param("id") Long id);
    
    /**
     * 查询所有分类列表
     */
    List<ArticleCategory> selectAll(@Param("status") Integer status);
    
    /**
     * 插入分类
     */
    int insert(ArticleCategory category);
    
    /**
     * 更新分类信息
     */
    int updateById(ArticleCategory category);
    
    /**
     * 删除分类
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 检查分类下是否有文章
     */
    long countArticlesByCategoryId(@Param("categoryId") Long categoryId);
}
