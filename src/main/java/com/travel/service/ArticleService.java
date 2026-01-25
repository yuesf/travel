package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.common.ResultCode;
import com.travel.dto.ArticleCreateRequest;
import com.travel.dto.ArticleListRequest;
import com.travel.dto.ArticleUpdateRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Article;
import com.travel.entity.ArticleImage;
import com.travel.entity.ArticleTagRelation;
import com.travel.entity.UserArticleFavorite;
import com.travel.entity.UserArticleLike;
import com.travel.exception.BusinessException;
import com.travel.mapper.ArticleMapper;
import com.travel.mapper.ArticleImageMapper;
import com.travel.mapper.ArticleTagRelationMapper;
import com.travel.mapper.UserArticleFavoriteMapper;
import com.travel.mapper.UserArticleLikeMapper;
import com.travel.util.OssUrlUtil;
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
    private ArticleImageMapper articleImageMapper;
    
    @Autowired
    private ArticleTagRelationMapper articleTagRelationMapper;
    
    @Autowired
    private UserArticleLikeMapper userArticleLikeMapper;
    
    @Autowired
    private UserArticleFavoriteMapper userArticleFavoriteMapper;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    @Qualifier("articleDetailCache")
    private Cache<Long, Article> articleDetailCache;
    
    @Autowired
    @Qualifier("articleViewCache")
    private Cache<String, Long> articleViewCache;
    
    @Autowired
    private OssUrlUtil ossUrlUtil;
    
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
        
        // 加载文章图片列表
        List<ArticleImage> images = articleImageMapper.selectByArticleId(id);
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = images.stream()
                .map(ArticleImage::getImageUrl)
                .collect(Collectors.toList());
            article.setImages(imageUrls);
        }
        
        // 处理OSS URL签名（用于管理后台预览）
        processOssUrlsInArticle(article);
        
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
        
        // 自动刷新缓存
        try {
            cacheService.evictArticleDetail(article.getId());
            cacheService.evictHome();
            log.info("文章创建成功后自动清除缓存，ID: {}", article.getId());
        } catch (Exception e) {
            log.error("清除文章缓存失败，但不影响文章创建，ID: {}", article.getId(), e);
        }
        
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
        
        // 清除文章详情缓存，确保小程序端获取最新数据
        articleDetailCache.invalidate(id);
        log.info("已清除文章详情缓存: id={}", id);
        
        log.info("更新文章成功: id={}, title={}", article.getId(), article.getTitle());
        
        // 自动刷新缓存（状态变更时清除首页缓存）
        try {
            if (request.getStatus() != null) {
                cacheService.evictHome();
            }
            log.info("文章更新成功后自动清除缓存，ID: {}", id);
        } catch (Exception e) {
            log.error("清除文章缓存失败，但不影响文章更新，ID: {}", id, e);
        }
        
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
        
        // 自动刷新缓存
        try {
            cacheService.evictHome();
            log.info("文章删除成功后自动清除缓存，ID: {}", id);
        } catch (Exception e) {
            log.error("清除文章缓存失败，但不影响文章删除，ID: {}", id, e);
        }
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
        
        // 处理OSS URL签名（返回的URL都是签名URL）
        processOssUrlsInArticles(list);
        
        // 查询总数
        long total = articleMapper.countForMiniProgram(categoryId, tagId, keyword);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 小程序：根据ID查询文章详情（带缓存）
     * 注意：返回的图片URL（coverImage、images字段）都是签名URL（OSS文件），可直接使用
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
            // 加载文章图片列表
            List<ArticleImage> images = articleImageMapper.selectByArticleId(key);
            if (images != null && !images.isEmpty()) {
                List<String> imageUrls = images.stream()
                    .map(ArticleImage::getImageUrl)
                    .collect(Collectors.toList());
                a.setImages(imageUrls);
            }
            return a;
        });
        
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 如果缓存中的文章没有图片，重新加载图片（兼容旧缓存）
        if (article.getImages() == null) {
            List<ArticleImage> images = articleImageMapper.selectByArticleId(id);
            if (images != null && !images.isEmpty()) {
                List<String> imageUrls = images.stream()
                    .map(ArticleImage::getImageUrl)
                    .collect(Collectors.toList());
                article.setImages(imageUrls);
            } else {
                article.setImages(List.of());
            }
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
        
        // 处理OSS URL签名（返回的URL都是签名URL）
        processOssUrlsInArticle(article);
        
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
        
        // 处理OSS URL签名（返回的URL都是签名URL）
        processOssUrlsInArticles(list);
        
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
        
        // 处理OSS URL签名（返回的URL都是签名URL）
        processOssUrlsInArticles(articles);
        
        return new PageResult<>(articles, total, page, pageSize);
    }
    
    // ==================== 文章图片管理方法 ====================
    
    /**
     * 获取文章图片列表
     */
    public List<ArticleImage> getArticleImages(Long articleId) {
        if (articleId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return articleImageMapper.selectByArticleId(articleId);
    }
    
    /**
     * 保存文章图片（创建或更新）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveArticleImages(Long articleId, List<String> imageUrls) {
        if (articleId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 验证文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 验证图片数量（最多10张）
        if (imageUrls != null && imageUrls.size() > 10) {
            throw new BusinessException("图片数量不能超过10张");
        }
        
        // 删除原有图片
        articleImageMapper.deleteByArticleId(articleId);
        
        // 插入新图片
        if (imageUrls != null && !imageUrls.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < imageUrls.size(); i++) {
                ArticleImage image = new ArticleImage();
                image.setArticleId(articleId);
                image.setImageUrl(imageUrls.get(i));
                image.setSort(i);
                image.setCreateTime(now);
                image.setUpdateTime(now);
                articleImageMapper.insert(image);
            }
        }
        
        // 清除文章详情缓存
        articleDetailCache.invalidate(articleId);
        log.info("保存文章图片成功: articleId={}, imageCount={}", articleId, 
                imageUrls != null ? imageUrls.size() : 0);
    }
    
    /**
     * 删除单张图片
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticleImage(Long imageId) {
        if (imageId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 查询图片记录获取文章ID
        ArticleImage image = articleImageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        Long articleId = image.getArticleId();
        
        // 删除图片记录
        int result = articleImageMapper.deleteById(imageId);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        // 清除文章详情缓存
        articleDetailCache.invalidate(articleId);
        log.info("删除文章图片成功: imageId={}, articleId={}", imageId, articleId);
    }
    
    /**
     * 更新图片排序
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateImageSort(Long imageId, Integer sort) {
        if (imageId == null || sort == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 查询图片记录获取文章ID
        ArticleImage image = articleImageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        Long articleId = image.getArticleId();
        
        // 更新排序
        int result = articleImageMapper.updateSort(imageId, sort);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        // 清除文章详情缓存
        articleDetailCache.invalidate(articleId);
        log.info("更新图片排序成功: imageId={}, sort={}, articleId={}", imageId, sort, articleId);
    }
    
    /**
     * 处理文章中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInArticle(Article article) {
        if (article == null) {
            return;
        }
        // 处理封面图
        if (article.getCoverImage() != null && !article.getCoverImage().isEmpty()) {
            article.setCoverImage(ossUrlUtil.processUrl(article.getCoverImage()));
        }
        // 处理图片列表（images字段是List<String>，需要特殊处理）
        if (article.getImages() != null && !article.getImages().isEmpty()) {
            List<String> signedImages = new java.util.ArrayList<>();
            for (String imageUrl : article.getImages()) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    signedImages.add(ossUrlUtil.processUrl(imageUrl));
                } else {
                    signedImages.add(imageUrl);
                }
            }
            article.setImages(signedImages);
        }
        // 处理文章内容中的图片URL（HTML格式）
        if (article.getContent() != null && !article.getContent().isEmpty()) {
            String content = processOssUrlsInHtml(article.getContent());
            article.setContent(content);
        }
    }
    
    /**
     * 处理HTML内容中的OSS图片URL，生成签名URL
     * 
     * @param html HTML内容
     * @return 处理后的HTML内容
     */
    private String processOssUrlsInHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        
        // 使用正则表达式匹配img标签中的src属性
        // 匹配格式：<img ... src="url" ...> 或 <img ... src='url' ...>
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(<img[^>]+src\\s*=\\s*[\"'])([^\"']+)([\"'][^>]*>)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        
        java.util.regex.Matcher matcher = pattern.matcher(html);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String prefix = matcher.group(1); // <img ... src="
            String imageUrl = matcher.group(2); // 图片URL（可能是URL编码的）
            String suffix = matcher.group(3); // " ...>
            
            try {
                // 先URL解码，处理HTML实体编码和URL编码
                String decodedUrl = java.net.URLDecoder.decode(imageUrl, "UTF-8");
                
                // 如果URL已经包含查询参数（签名参数），先提取基础URL
                String baseUrl = decodedUrl;
                int queryIndex = decodedUrl.indexOf('?');
                if (queryIndex > 0) {
                    // 去掉查询参数，只保留基础URL
                    baseUrl = decodedUrl.substring(0, queryIndex);
                    log.debug("检测到URL已包含查询参数，提取基础URL: {} -> {}", 
                        decodedUrl.length() > 80 ? decodedUrl.substring(0, 80) + "..." : decodedUrl,
                        baseUrl);
                }
                
                // 处理基础URL，如果是OSS URL则生成新的签名URL
                String signedUrl = ossUrlUtil.processUrl(baseUrl);
                
                // 如果生成了新的签名URL，使用新URL；否则使用解码后的原URL
                String finalUrl = signedUrl != null && !signedUrl.equals(baseUrl) ? signedUrl : decodedUrl;
                
                // 替换为签名URL（需要对特殊字符进行HTML转义）
                String escapedUrl = finalUrl.replace("&", "&amp;");
                matcher.appendReplacement(result, prefix + escapedUrl + suffix);
                
            } catch (java.io.UnsupportedEncodingException e) {
                log.warn("URL解码失败，使用原URL: {}", imageUrl, e);
                // 解码失败，使用原URL
                matcher.appendReplacement(result, matcher.group(0));
            } catch (Exception e) {
                log.warn("处理图片URL失败，使用原URL: {}", imageUrl, e);
                // 处理失败，使用原URL
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 处理文章列表中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInArticles(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        for (Article article : articles) {
            processOssUrlsInArticle(article);
        }
    }
}
