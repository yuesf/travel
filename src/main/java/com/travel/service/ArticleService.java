package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.common.ResultCode;
import com.travel.dto.ArticleCreateRequest;
import com.travel.dto.ArticleListRequest;
import com.travel.dto.ArticleUpdateRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Article;
import com.travel.entity.ArticleTagRelation;
import com.travel.entity.UserArticleFavorite;
import com.travel.entity.UserArticleLike;
import com.travel.exception.BusinessException;
import com.travel.mapper.ArticleMapper;
import com.travel.mapper.ArticleTagRelationMapper;
import com.travel.mapper.UserArticleFavoriteMapper;
import com.travel.mapper.UserArticleLikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class ArticleService {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private ArticleTagRelationMapper articleTagRelationMapper;
    
    @Autowired
    private UserArticleLikeMapper userArticleLikeMapper;
    
    @Autowired
    private UserArticleFavoriteMapper userArticleFavoriteMapper;
    
    @Autowired
    @Qualifier("articleDetailCache")
    private Cache<Long, Article> articleDetailCache;
    
    @Autowired
    @Qualifier("articleViewCache")
    private Cache<String, Long> articleViewCache;
    
    /**
     * 分页查询文章列表
     */
    public PageResult<Article> list(ArticleListRequest request) {
        // 参数校验
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getPageSize();
        
        // 查询列表
        List<Article> list = articleMapper.selectList(
            request.getTitle(),
            request.getCategoryId(),
            request.getAuthor(),
            request.getStatus(),
            request.getPublishTimeStart(),
            request.getPublishTimeEnd(),
            offset,
            request.getPageSize()
        );
        
        // 查询总数
        long total = articleMapper.count(
            request.getTitle(),
            request.getCategoryId(),
            request.getAuthor(),
            request.getStatus(),
            request.getPublishTimeStart(),
            request.getPublishTimeEnd()
        );
        
        return new PageResult<>(list, total, request.getPage(), request.getPageSize());
    }
    
    /**
     * 根据ID查询文章详情
     */
    public Article getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 加载标签ID列表
        List<ArticleTagRelation> relations = articleTagRelationMapper.selectByArticleId(id);
        if (relations != null && !relations.isEmpty()) {
            List<Long> tagIds = relations.stream()
                .map(ArticleTagRelation::getTagId)
                .collect(Collectors.toList());
            article.setTagIds(tagIds);
        }
        
        return article;
    }
    
    /**
     * 创建文章
     */
    @Transactional(rollbackFor = Exception.class)
    public Article create(ArticleCreateRequest request, Long authorId) {
        // 创建文章实体
        Article article = new Article();
        BeanUtils.copyProperties(request, article);
        
        // 设置作者ID
        article.setAuthorId(authorId);
        
        // 设置默认值
        if (article.getStatus() == null) {
            article.setStatus(0); // 默认草稿
        }
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getLikeCount() == null) {
            article.setLikeCount(0);
        }
        if (article.getFavoriteCount() == null) {
            article.setFavoriteCount(0);
        }
        if (article.getIsRecommend() == null) {
            article.setIsRecommend(0);
        }
        if (article.getSort() == null) {
            article.setSort(0);
        }
        
        // 如果状态是已发布，设置发布时间
        if (article.getStatus() == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        // 插入数据库
        int result = articleMapper.insert(article);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        // 处理标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ArticleTagRelation relation = new ArticleTagRelation();
                relation.setArticleId(article.getId());
                relation.setTagId(tagId);
                articleTagRelationMapper.insert(relation);
            }
        }
        
        log.info("创建文章成功: id={}, title={}", article.getId(), article.getTitle());
        
        return article;
    }
    
    /**
     * 更新文章
     */
    @Transactional(rollbackFor = Exception.class)
    public Article update(Long id, ArticleUpdateRequest request) {
        // 检查文章是否存在
        Article article = getById(id);
        
        // 更新字段
        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }
        if (request.getCoverImage() != null) {
            article.setCoverImage(request.getCoverImage());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getCategoryId() != null) {
            article.setCategoryId(request.getCategoryId());
        }
        if (request.getAuthor() != null) {
            article.setAuthor(request.getAuthor());
        }
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
            // 如果状态改为已发布，且发布时间为空，设置发布时间
            if (request.getStatus() == 1 && article.getPublishTime() == null) {
                article.setPublishTime(LocalDateTime.now());
            }
        }
        if (request.getPublishTime() != null) {
            article.setPublishTime(request.getPublishTime());
        }
        if (request.getIsRecommend() != null) {
            article.setIsRecommend(request.getIsRecommend());
        }
        if (request.getSort() != null) {
            article.setSort(request.getSort());
        }
        
        // 更新数据库
        int result = articleMapper.updateById(article);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        // 更新标签关联
        if (request.getTagIds() != null) {
            // 删除原有标签关联
            articleTagRelationMapper.deleteByArticleId(id);
            // 添加新标签关联
            for (Long tagId : request.getTagIds()) {
                ArticleTagRelation relation = new ArticleTagRelation();
                relation.setArticleId(id);
                relation.setTagId(tagId);
                articleTagRelationMapper.insert(relation);
            }
        }
        
        log.info("更新文章成功: id={}, title={}", article.getId(), article.getTitle());
        
        return article;
    }
    
    /**
     * 删除文章（软删除，改为已下架）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查文章是否存在
        Article article = getById(id);
        
        // 软删除：将状态改为已下架
        ArticleUpdateRequest updateRequest = new ArticleUpdateRequest();
        updateRequest.setStatus(2);
        update(id, updateRequest);
        
        log.info("删除文章成功（软删除）: id={}, title={}", article.getId(), article.getTitle());
    }
    
    /**
     * 批量删除文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        for (Long id : ids) {
            delete(id);
        }
        
        log.info("批量删除文章成功: ids={}", ids);
    }
    
    /**
     * 增加阅读量
     */
    public void incrementViewCount(Long id) {
        articleMapper.incrementViewCount(id);
    }
    
    // ==================== 小程序端方法 ====================
    
    /**
     * 小程序：分页查询文章列表（支持分类、标签筛选，排序）
     */
    public PageResult<Article> listForMiniProgram(Long categoryId, Long tagId, String keyword, 
                                                   String sortType, Integer page, Integer pageSize) {
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 50) {
            pageSize = 50; // 限制最大每页数量
        }
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询列表
        List<Article> list = articleMapper.selectListForMiniProgram(
            categoryId, tagId, keyword, sortType, offset, pageSize
        );
        
        // 加载标签ID列表
        for (Article article : list) {
            List<ArticleTagRelation> relations = articleTagRelationMapper.selectByArticleId(article.getId());
            if (relations != null && !relations.isEmpty()) {
                List<Long> tagIds = relations.stream()
                    .map(ArticleTagRelation::getTagId)
                    .collect(Collectors.toList());
                article.setTagIds(tagIds);
            }
        }
        
        // 查询总数
        long total = articleMapper.countForMiniProgram(categoryId, tagId, keyword);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 小程序：根据ID查询文章详情（带缓存）
     */
    public Article getDetailForMiniProgram(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 先从缓存获取
        Article article = articleDetailCache.get(id, key -> {
            Article a = articleMapper.selectById(key);
            if (a == null || a.getStatus() != 1) {
                return null;
            }
            // 加载标签ID列表
            List<ArticleTagRelation> relations = articleTagRelationMapper.selectByArticleId(key);
            if (relations != null && !relations.isEmpty()) {
                List<Long> tagIds = relations.stream()
                    .map(ArticleTagRelation::getTagId)
                    .collect(Collectors.toList());
                a.setTagIds(tagIds);
            }
            return a;
        });
        
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 防刷机制：同一用户短时间内多次访问只统计一次
        String viewKey = userId != null ? userId + ":" + id : "anonymous:" + id;
        Long lastViewTime = articleViewCache.getIfPresent(viewKey);
        long currentTime = System.currentTimeMillis();
        
        // 如果5分钟内没有访问过，才增加阅读量
        if (lastViewTime == null || (currentTime - lastViewTime) > 5 * 60 * 1000) {
            articleMapper.incrementViewCount(id);
            articleViewCache.put(viewKey, currentTime);
            // 更新缓存中的文章阅读量
            article.setViewCount(article.getViewCount() + 1);
            articleDetailCache.put(id, article);
        }
        
        // 如果用户已登录，检查是否点赞和收藏
        // 注意：这里可以扩展Article实体类添加isLiked和isFavorited字段
        // 或者通过DTO返回这些信息，当前暂不实现
        
        return article;
    }
    
    /**
     * 小程序：搜索文章
     */
    public PageResult<Article> searchArticles(String keyword, Integer page, Integer pageSize) {
        return listForMiniProgram(null, null, keyword, null, page, pageSize);
    }
    
    /**
     * 小程序：获取推荐文章列表
     */
    public List<Article> getRecommendList(Integer limit) {
        if (limit == null || limit < 1) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        List<Article> list = articleMapper.selectRecommendList(limit);
        
        // 加载标签ID列表
        for (Article article : list) {
            List<ArticleTagRelation> relations = articleTagRelationMapper.selectByArticleId(article.getId());
            if (relations != null && !relations.isEmpty()) {
                List<Long> tagIds = relations.stream()
                    .map(ArticleTagRelation::getTagId)
                    .collect(Collectors.toList());
                article.setTagIds(tagIds);
            }
        }
        
        return list;
    }
    
    /**
     * 小程序：获取相关文章推荐
     */
    public List<Article> getRelatedArticles(Long articleId, Integer limit) {
        if (articleId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 先获取当前文章信息
        Article currentArticle = articleMapper.selectById(articleId);
        if (currentArticle == null || currentArticle.getCategoryId() == null) {
            return List.of();
        }
        
        if (limit == null || limit < 1) {
            limit = 5;
        }
        if (limit > 20) {
            limit = 20;
        }
        
        List<Article> list = articleMapper.selectRelatedList(
            currentArticle.getCategoryId(), articleId, limit
        );
        
        // 加载标签ID列表
        for (Article article : list) {
            List<ArticleTagRelation> relations = articleTagRelationMapper.selectByArticleId(article.getId());
            if (relations != null && !relations.isEmpty()) {
                List<Long> tagIds = relations.stream()
                    .map(ArticleTagRelation::getTagId)
                    .collect(Collectors.toList());
                article.setTagIds(tagIds);
            }
        }
        
        return list;
    }
    
    /**
     * 小程序：点赞文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId, Long userId) {
        if (articleId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 检查是否已点赞
        int count = userArticleLikeMapper.countByUserIdAndArticleId(userId, articleId);
        if (count > 0) {
            throw new BusinessException("您已经点赞过这篇文章");
        }
        
        // 添加点赞记录
        UserArticleLike like = new UserArticleLike();
        like.setUserId(userId);
        like.setArticleId(articleId);
        userArticleLikeMapper.insert(like);
        
        // 增加点赞量
        articleMapper.incrementLikeCount(articleId);
        
        // 清除缓存
        articleDetailCache.invalidate(articleId);
        
        log.info("用户点赞文章成功: userId={}, articleId={}", userId, articleId);
    }
    
    /**
     * 小程序：取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId, Long userId) {
        if (articleId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 检查是否已点赞
        int count = userArticleLikeMapper.countByUserIdAndArticleId(userId, articleId);
        if (count == 0) {
            throw new BusinessException("您还没有点赞过这篇文章");
        }
        
        // 删除点赞记录
        userArticleLikeMapper.delete(userId, articleId);
        
        // 减少点赞量
        articleMapper.decrementLikeCount(articleId);
        
        // 清除缓存
        articleDetailCache.invalidate(articleId);
        
        log.info("用户取消点赞成功: userId={}, articleId={}", userId, articleId);
    }
    
    /**
     * 小程序：收藏文章
     */
    @Transactional(rollbackFor = Exception.class)
    public void favoriteArticle(Long articleId, Long userId) {
        if (articleId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getStatus() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 检查是否已收藏
        int count = userArticleFavoriteMapper.countByUserIdAndArticleId(userId, articleId);
        if (count > 0) {
            throw new BusinessException("您已经收藏过这篇文章");
        }
        
        // 添加收藏记录
        UserArticleFavorite favorite = new UserArticleFavorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        userArticleFavoriteMapper.insert(favorite);
        
        // 增加收藏量
        articleMapper.incrementFavoriteCount(articleId);
        
        // 清除缓存
        articleDetailCache.invalidate(articleId);
        
        log.info("用户收藏文章成功: userId={}, articleId={}", userId, articleId);
    }
    
    /**
     * 小程序：取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteArticle(Long articleId, Long userId) {
        if (articleId == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 检查是否已收藏
        int count = userArticleFavoriteMapper.countByUserIdAndArticleId(userId, articleId);
        if (count == 0) {
            throw new BusinessException("您还没有收藏过这篇文章");
        }
        
        // 删除收藏记录
        userArticleFavoriteMapper.delete(userId, articleId);
        
        // 减少收藏量
        articleMapper.decrementFavoriteCount(articleId);
        
        // 清除缓存
        articleDetailCache.invalidate(articleId);
        
        log.info("用户取消收藏成功: userId={}, articleId={}", userId, articleId);
    }
    
    /**
     * 小程序：获取用户收藏的文章列表（分页）
     */
    public PageResult<Article> getUserFavoriteArticles(Long userId, Integer page, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询收藏的文章ID列表
        List<Long> articleIds = userArticleFavoriteMapper.selectArticleIdsByUserId(userId, offset, pageSize);
        
        // 查询总数
        long total = userArticleFavoriteMapper.countByUserId(userId);
        
        // 如果没有收藏，返回空列表
        if (articleIds == null || articleIds.isEmpty()) {
            return new PageResult<>(List.of(), total, page, pageSize);
        }
        
        // 根据ID列表查询文章详情
        List<Article> articles = articleMapper.selectByIds(articleIds);
        
        // 加载标签ID列表
        for (Article article : articles) {
            List<ArticleTagRelation> relations = articleTagRelationMapper.selectByArticleId(article.getId());
            if (relations != null && !relations.isEmpty()) {
                List<Long> tagIds = relations.stream()
                    .map(ArticleTagRelation::getTagId)
                    .collect(Collectors.toList());
                article.setTagIds(tagIds);
            }
        }
        
        return new PageResult<>(articles, total, page, pageSize);
    }
}
