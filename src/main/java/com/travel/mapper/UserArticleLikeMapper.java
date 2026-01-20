package com.travel.mapper;

import com.travel.entity.UserArticleLike;
import org.apache.ibatis.annotations.Param;

/**
 * 用户文章点赞Mapper接口
 * 
 * @author travel-platform
 */
public interface UserArticleLikeMapper {
    
    /**
     * 插入点赞记录
     */
    int insert(UserArticleLike like);
    
    /**
     * 删除点赞记录
     */
    int delete(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    /**
     * 检查用户是否已点赞
     */
    int countByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
