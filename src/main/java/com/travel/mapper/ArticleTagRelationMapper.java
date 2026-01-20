package com.travel.mapper;

import com.travel.entity.ArticleTagRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章标签关联Mapper接口
 * 
 * @author travel-platform
 */
public interface ArticleTagRelationMapper {
    
    /**
     * 根据文章ID查询标签关联列表
     */
    List<ArticleTagRelation> selectByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 根据标签ID查询文章关联列表
     */
    List<ArticleTagRelation> selectByTagId(@Param("tagId") Long tagId);
    
    /**
     * 插入标签关联
     */
    int insert(ArticleTagRelation relation);
    
    /**
     * 删除文章的所有标签关联
     */
    int deleteByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 删除指定标签关联
     */
    int delete(@Param("articleId") Long articleId, @Param("tagId") Long tagId);
    
    /**
     * 检查关联是否存在
     */
    int countByArticleIdAndTagId(@Param("articleId") Long articleId, @Param("tagId") Long tagId);
}
