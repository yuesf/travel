package com.travel.mapper;

import com.travel.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章Mapper接口
 * 
 * @author travel-platform
 */
public interface ArticleMapper {
    
    /**
     * 根据ID查询文章
     */
    Article selectById(@Param("id") Long id);
    
    /**
     * 分页查询文章列表
     */
    List<Article> selectList(@Param("title") String title,
                            @Param("categoryId") Long categoryId,
                            @Param("author") String author,
                            @Param("status") Integer status,
                            @Param("publishTimeStart") String publishTimeStart,
                            @Param("publishTimeEnd") String publishTimeEnd,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);
    
    /**
     * 统计文章总数
     */
    long count(@Param("title") String title,
              @Param("categoryId") Long categoryId,
              @Param("author") String author,
              @Param("status") Integer status,
              @Param("publishTimeStart") String publishTimeStart,
              @Param("publishTimeEnd") String publishTimeEnd);
    
    /**
     * 插入文章
     */
    int insert(Article article);
    
    /**
     * 更新文章信息
     */
    int updateById(Article article);
    
    /**
     * 删除文章（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 批量删除文章
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 增加阅读量
     */
    int incrementViewCount(@Param("id") Long id);
    
    /**
     * 增加点赞量
     */
    int incrementLikeCount(@Param("id") Long id);
    
    /**
     * 减少点赞量
     */
    int decrementLikeCount(@Param("id") Long id);
    
    /**
     * 增加收藏量
     */
    int incrementFavoriteCount(@Param("id") Long id);
    
    /**
     * 减少收藏量
     */
    int decrementFavoriteCount(@Param("id") Long id);
    
    /**
     * 小程序：分页查询文章列表（支持分类、标签筛选，排序）
     */
    List<Article> selectListForMiniProgram(@Param("categoryId") Long categoryId,
                                          @Param("tagId") Long tagId,
                                          @Param("keyword") String keyword,
                                          @Param("sortType") String sortType,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);
    
    /**
     * 小程序：统计文章总数（支持分类、标签筛选）
     */
    long countForMiniProgram(@Param("categoryId") Long categoryId,
                            @Param("tagId") Long tagId,
                            @Param("keyword") String keyword);
    
    /**
     * 小程序：获取推荐文章列表
     */
    List<Article> selectRecommendList(@Param("limit") Integer limit);
    
    /**
     * 小程序：获取相关文章推荐（同分类，排除当前文章）
     */
    List<Article> selectRelatedList(@Param("categoryId") Long categoryId,
                                   @Param("excludeArticleId") Long excludeArticleId,
                                   @Param("limit") Integer limit);
    
    /**
     * 根据ID列表查询文章
     */
    List<Article> selectByIds(@Param("ids") List<Long> ids);
}
