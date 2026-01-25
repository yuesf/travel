package com.travel.mapper;

import com.travel.entity.ArticleImage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章图片Mapper接口
 * 
 * @author travel-platform
 */
public interface ArticleImageMapper {
    
    /**
     * 根据文章ID查询图片列表（按排序字段排序）
     */
    List<ArticleImage> selectByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 根据ID查询图片
     */
    ArticleImage selectById(@Param("id") Long id);
    
    /**
     * 插入图片记录
     */
    int insert(ArticleImage image);
    
    /**
     * 根据ID删除图片记录
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据文章ID删除所有图片记录
     */
    int deleteByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 更新图片排序
     */
    int updateSort(@Param("id") Long id, @Param("sort") Integer sort);
}
