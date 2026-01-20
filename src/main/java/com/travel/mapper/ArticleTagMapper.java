package com.travel.mapper;

import com.travel.entity.ArticleTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章标签Mapper接口
 * 
 * @author travel-platform
 */
public interface ArticleTagMapper {
    
    /**
     * 根据ID查询标签
     */
    ArticleTag selectById(@Param("id") Long id);
    
    /**
     * 根据名称查询标签
     */
    ArticleTag selectByName(@Param("name") String name);
    
    /**
     * 查询所有标签列表
     */
    List<ArticleTag> selectAll();
    
    /**
     * 插入标签
     */
    int insert(ArticleTag tag);
    
    /**
     * 更新标签信息
     */
    int updateById(ArticleTag tag);
    
    /**
     * 删除标签
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 增加使用次数
     */
    int incrementUseCount(@Param("id") Long id);
    
    /**
     * 减少使用次数
     */
    int decrementUseCount(@Param("id") Long id);
}
