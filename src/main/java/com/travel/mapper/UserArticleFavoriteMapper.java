package com.travel.mapper;

import com.travel.entity.UserArticleFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户文章收藏Mapper接口
 * 
 * @author travel-platform
 */
public interface UserArticleFavoriteMapper {
    
    /**
     * 插入收藏记录
     */
    int insert(UserArticleFavorite favorite);
    
    /**
     * 删除收藏记录
     */
    int delete(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    /**
     * 检查用户是否已收藏
     */
    int countByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    /**
     * 查询用户的收藏文章ID列表（分页）
     */
    List<Long> selectArticleIdsByUserId(@Param("userId") Long userId, 
                                       @Param("offset") Integer offset, 
                                       @Param("limit") Integer limit);
    
    /**
     * 统计用户的收藏文章总数
     */
    long countByUserId(@Param("userId") Long userId);
}
