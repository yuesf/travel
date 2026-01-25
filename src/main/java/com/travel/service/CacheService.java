package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.common.CacheConstants;
import com.travel.dto.HomeResponse;
import com.travel.entity.Article;
import com.travel.entity.Attraction;
import com.travel.entity.MiniProgramConfig;
import com.travel.entity.PaymentConfig;
import com.travel.entity.Product;
import com.travel.mapper.MiniProgramConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 缓存管理服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class CacheService {
    
    @Autowired
    @Qualifier("tokenBlacklistCache")
    private Cache<String, String> tokenBlacklistCache;
    
    @Autowired
    @Qualifier("smsCodeCache")
    private Cache<String, String> smsCodeCache;
    
    @Autowired
    @Qualifier("miniprogramConfigCache")
    private Cache<String, MiniProgramConfig> miniprogramConfigCache;
    
    @Autowired
    @Qualifier("homeCache")
    private Cache<String, HomeResponse> homeCache;
    
    @Autowired
    @Qualifier("attractionDetailCache")
    private Cache<Long, Attraction> attractionDetailCache;
    
    @Autowired
    @Qualifier("productDetailCache")
    private Cache<Long, Object> productDetailCache;
    
    @Autowired
    @Qualifier("articleDetailCache")
    private Cache<Long, Article> articleDetailCache;
    
    @Autowired
    @Qualifier("articleViewCache")
    private Cache<String, Long> articleViewCache;
    
    @Autowired
    @Lazy
    private HomeService homeService;
    
    @Autowired
    private MiniProgramConfigMapper miniProgramConfigMapper;
    
    @Autowired
    @Lazy
    private PaymentConfigService paymentConfigService;
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    @Autowired
    private com.travel.mapper.ProductMapper productMapper;
    
    @Autowired
    private com.travel.mapper.ArticleMapper articleMapper;
    
    @Autowired
    private com.travel.mapper.AttractionMapper attractionMapper;
    
    /**
     * 清除所有缓存
     */
    public void clearAll() {
        tokenBlacklistCache.invalidateAll();
        smsCodeCache.invalidateAll();
        miniprogramConfigCache.invalidateAll();
        homeCache.invalidateAll();
        attractionDetailCache.invalidateAll();
        productDetailCache.invalidateAll();
        articleDetailCache.invalidateAll();
        articleViewCache.invalidateAll();
        log.info("已清除所有缓存");
    }
    
    /**
     * 清除指定类型的缓存
     * 
     * @param cacheType 缓存类型
     */
    public void clearByType(String cacheType) {
        switch (cacheType) {
            case "token":
                tokenBlacklistCache.invalidateAll();
                log.info("已清除Token黑名单缓存");
                break;
            case "sms":
                smsCodeCache.invalidateAll();
                log.info("已清除短信验证码缓存");
                break;
            case "miniprogram":
                miniprogramConfigCache.invalidateAll();
                log.info("已清除小程序配置缓存");
                break;
            case "home":
                homeCache.invalidateAll();
                log.info("已清除首页数据缓存");
                break;
            case "attraction":
                attractionDetailCache.invalidateAll();
                log.info("已清除景点详情缓存");
                break;
            case "product":
                productDetailCache.invalidateAll();
                log.info("已清除商品详情缓存");
                break;
            case "article":
                articleDetailCache.invalidateAll();
                log.info("已清除文章详情缓存");
                break;
            case "articleView":
                articleViewCache.invalidateAll();
                log.info("已清除文章阅读量缓存");
                break;
            default:
                throw new IllegalArgumentException("未知的缓存类型: " + cacheType);
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        var tokenStats = tokenBlacklistCache.stats();
        stats.put("tokenBlacklist", Map.of(
            "estimatedSize", tokenBlacklistCache.estimatedSize(),
            "hitCount", tokenStats.hitCount(),
            "missCount", tokenStats.missCount(),
            "evictionCount", tokenStats.evictionCount()
        ));
        
        var smsStats = smsCodeCache.stats();
        stats.put("smsCode", Map.of(
            "estimatedSize", smsCodeCache.estimatedSize(),
            "hitCount", smsStats.hitCount(),
            "missCount", smsStats.missCount(),
            "evictionCount", smsStats.evictionCount()
        ));
        
        var miniprogramStats = miniprogramConfigCache.stats();
        stats.put("miniprogramConfig", Map.of(
            "estimatedSize", miniprogramConfigCache.estimatedSize(),
            "hitCount", miniprogramStats.hitCount(),
            "missCount", miniprogramStats.missCount(),
            "evictionCount", miniprogramStats.evictionCount()
        ));
        
        var homeStats = homeCache.stats();
        stats.put("home", Map.of(
            "estimatedSize", homeCache.estimatedSize(),
            "hitCount", homeStats.hitCount(),
            "missCount", homeStats.missCount(),
            "evictionCount", homeStats.evictionCount()
        ));
        
        var attractionStats = attractionDetailCache.stats();
        stats.put("attractionDetail", Map.of(
            "estimatedSize", attractionDetailCache.estimatedSize(),
            "hitCount", attractionStats.hitCount(),
            "missCount", attractionStats.missCount(),
            "evictionCount", attractionStats.evictionCount()
        ));
        
        var productStats = productDetailCache.stats();
        stats.put("productDetail", Map.of(
            "estimatedSize", productDetailCache.estimatedSize(),
            "hitCount", productStats.hitCount(),
            "missCount", productStats.missCount(),
            "evictionCount", productStats.evictionCount()
        ));
        
        var articleStats = articleDetailCache.stats();
        stats.put("articleDetail", Map.of(
            "estimatedSize", articleDetailCache.estimatedSize(),
            "hitCount", articleStats.hitCount(),
            "missCount", articleStats.missCount(),
            "evictionCount", articleStats.evictionCount()
        ));
        
        var articleViewStats = articleViewCache.stats();
        stats.put("articleView", Map.of(
            "estimatedSize", articleViewCache.estimatedSize(),
            "hitCount", articleViewStats.hitCount(),
            "missCount", articleViewStats.missCount(),
            "evictionCount", articleViewStats.evictionCount()
        ));
        
        return stats;
    }
    
    // ==================== 细粒度缓存清除方法 ====================
    
    /**
     * 清除首页缓存
     */
    public void evictHome() {
        try {
            homeCache.invalidateAll();
            log.info("已清除首页缓存");
        } catch (Exception e) {
            log.error("清除首页缓存失败", e);
            throw new RuntimeException("清除首页缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除小程序配置缓存
     */
    public void evictMiniprogramConfig() {
        try {
            miniprogramConfigCache.invalidateAll();
            log.info("已清除小程序配置缓存");
        } catch (Exception e) {
            log.error("清除小程序配置缓存失败", e);
            throw new RuntimeException("清除小程序配置缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除特定商品缓存
     * 
     * @param id 商品ID
     */
    public void evictProductDetail(Long id) {
        try {
            if (id == null) {
                log.warn("商品ID为空，跳过清除缓存");
                return;
            }
            productDetailCache.invalidate(id);
            log.info("已清除商品缓存，ID: {}", id);
        } catch (Exception e) {
            log.error("清除商品缓存失败，ID: {}", id, e);
            throw new RuntimeException("清除商品缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除所有商品缓存
     */
    public void evictProductDetailAll() {
        try {
            productDetailCache.invalidateAll();
            log.info("已清除所有商品缓存");
        } catch (Exception e) {
            log.error("清除所有商品缓存失败", e);
            throw new RuntimeException("清除所有商品缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量清除商品缓存
     * 
     * @param ids 商品ID列表
     */
    public void evictProductDetails(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                log.warn("商品ID列表为空，跳过清除缓存");
                return;
            }
            ids.forEach(productDetailCache::invalidate);
            log.info("已批量清除商品缓存，数量: {}", ids.size());
        } catch (Exception e) {
            log.error("批量清除商品缓存失败", e);
            throw new RuntimeException("批量清除商品缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除特定文章缓存
     * 
     * @param id 文章ID
     */
    public void evictArticleDetail(Long id) {
        try {
            if (id == null) {
                log.warn("文章ID为空，跳过清除缓存");
                return;
            }
            articleDetailCache.invalidate(id);
            log.info("已清除文章缓存，ID: {}", id);
        } catch (Exception e) {
            log.error("清除文章缓存失败，ID: {}", id, e);
            throw new RuntimeException("清除文章缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除所有文章缓存
     */
    public void evictArticleDetailAll() {
        try {
            articleDetailCache.invalidateAll();
            log.info("已清除所有文章缓存");
        } catch (Exception e) {
            log.error("清除所有文章缓存失败", e);
            throw new RuntimeException("清除所有文章缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量清除文章缓存
     * 
     * @param ids 文章ID列表
     */
    public void evictArticleDetails(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                log.warn("文章ID列表为空，跳过清除缓存");
                return;
            }
            ids.forEach(articleDetailCache::invalidate);
            log.info("已批量清除文章缓存，数量: {}", ids.size());
        } catch (Exception e) {
            log.error("批量清除文章缓存失败", e);
            throw new RuntimeException("批量清除文章缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除特定景点缓存
     * 
     * @param id 景点ID
     */
    public void evictAttractionDetail(Long id) {
        try {
            if (id == null) {
                log.warn("景点ID为空，跳过清除缓存");
                return;
            }
            attractionDetailCache.invalidate(id);
            log.info("已清除景点缓存，ID: {}", id);
        } catch (Exception e) {
            log.error("清除景点缓存失败，ID: {}", id, e);
            throw new RuntimeException("清除景点缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除所有景点缓存
     */
    public void evictAttractionDetailAll() {
        try {
            attractionDetailCache.invalidateAll();
            log.info("已清除所有景点缓存");
        } catch (Exception e) {
            log.error("清除所有景点缓存失败", e);
            throw new RuntimeException("清除所有景点缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量清除景点缓存
     * 
     * @param ids 景点ID列表
     */
    public void evictAttractionDetails(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                log.warn("景点ID列表为空，跳过清除缓存");
                return;
            }
            ids.forEach(attractionDetailCache::invalidate);
            log.info("已批量清除景点缓存，数量: {}", ids.size());
        } catch (Exception e) {
            log.error("批量清除景点缓存失败", e);
            throw new RuntimeException("批量清除景点缓存失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量清除多种类型的缓存
     * 
     * @param cacheTypes 缓存类型列表
     */
    public void evictMultiple(List<String> cacheTypes) {
        if (cacheTypes == null || cacheTypes.isEmpty()) {
            log.warn("缓存类型列表为空，跳过清除缓存");
            return;
        }
        
        int successCount = 0;
        int failureCount = 0;
        
        for (String cacheType : cacheTypes) {
            try {
                clearByType(cacheType);
                successCount++;
            } catch (Exception e) {
                log.error("清除缓存失败，类型: {}", cacheType, e);
                failureCount++;
            }
        }
        
        log.info("批量清除缓存完成，成功: {}, 失败: {}", successCount, failureCount);
        
        if (failureCount > 0) {
            throw new RuntimeException(String.format("批量清除缓存部分失败，成功: %d, 失败: %d", successCount, failureCount));
        }
    }
    
    // ==================== 缓存预热方法（基础） ====================
    
    /**
     * 刷新首页缓存
     * 从数据库读取首页数据并加载到缓存
     * 
     * @return 缓存刷新结果
     */
    public CacheRefreshResult refreshHome() {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始刷新首页缓存");
            
            // 调用 HomeService 获取首页数据（会自动写入缓存）
            homeService.getHomeData();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("首页缓存刷新成功，耗时: {}ms", duration);
            
            return CacheRefreshResult.builder()
                    .totalCount(1)
                    .successCount(1)
                    .failedCount(0)
                    .duration(duration)
                    .build();
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("刷新首页缓存失败", e);
            
            return CacheRefreshResult.builder()
                    .totalCount(1)
                    .successCount(0)
                    .failedCount(1)
                    .duration(duration)
                    .build();
        }
    }
    
    /**
     * 刷新小程序配置缓存
     * 从数据库读取所有启用的小程序配置并加载到缓存
     * 
     * @return 缓存刷新结果
     */
    public CacheRefreshResult refreshMiniprogramConfig() {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始刷新小程序配置缓存");
            
            // 查询所有启用的配置
            List<MiniProgramConfig> configs = miniProgramConfigMapper.selectAll(null, 1);
            
            if (configs == null || configs.isEmpty()) {
                log.warn("未查询到启用的小程序配置");
                return CacheRefreshResult.builder()
                        .totalCount(0)
                        .successCount(0)
                        .failedCount(0)
                        .duration(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // 将配置写入缓存（按configKey作为键）
            int successCount = 0;
            int failedCount = 0;
            
            for (MiniProgramConfig config : configs) {
                try {
                    if (config.getConfigKey() != null) {
                        miniprogramConfigCache.put(config.getConfigKey(), config);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("写入小程序配置缓存失败，configKey: {}", config.getConfigKey(), e);
                    failedCount++;
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("小程序配置缓存刷新完成，成功: {}, 失败: {}, 耗时: {}ms", 
                    successCount, failedCount, duration);
            
            return CacheRefreshResult.builder()
                    .totalCount(configs.size())
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .duration(duration)
                    .build();
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("刷新小程序配置缓存失败", e);
            
            return CacheRefreshResult.builder()
                    .totalCount(0)
                    .successCount(0)
                    .failedCount(1)
                    .duration(duration)
                    .build();
        }
    }
    
    /**
     * 刷新支付配置缓存
     * 从数据库读取启用的支付配置并加载到缓存
     * 
     * @return 缓存刷新结果
     */
    public CacheRefreshResult refreshPaymentConfig() {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始刷新支付配置缓存");
            
            // 调用 PaymentConfigService 获取启用的配置（会自动写入缓存）
            PaymentConfig paymentConfig = paymentConfigService.getEnabled();
            
            if (paymentConfig == null) {
                log.warn("未查询到启用的支付配置");
                return CacheRefreshResult.builder()
                        .totalCount(0)
                        .successCount(0)
                        .failedCount(0)
                        .duration(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // 手动刷新 Spring Cache（清除后重新加载）
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache(CacheConstants.PAYMENT_CONFIG_CACHE);
                if (cache != null) {
                    cache.clear();
                    // 重新调用获取，触发缓存写入
                    paymentConfigService.getEnabled();
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("支付配置缓存刷新成功，耗时: {}ms", duration);
            
            return CacheRefreshResult.builder()
                    .totalCount(1)
                    .successCount(1)
                    .failedCount(0)
                    .duration(duration)
                    .build();
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("刷新支付配置缓存失败", e);
            
            return CacheRefreshResult.builder()
                    .totalCount(1)
                    .successCount(0)
                    .failedCount(1)
                    .duration(duration)
                    .build();
        }
    }
    
    // ==================== 批量缓存预热方法 ====================
    
    /**
     * 批量刷新商品缓存
     * 根据商品ID列表从数据库查询并加载到缓存
     * 
     * @param productIds 商品ID列表
     * @return 缓存刷新结果
     */
    public CacheRefreshResult refreshProducts(List<Long> productIds) {
        long startTime = System.currentTimeMillis();
        
        if (productIds == null || productIds.isEmpty()) {
            log.warn("商品ID列表为空，跳过刷新");
            return CacheRefreshResult.builder()
                    .totalCount(0)
                    .successCount(0)
                    .failedCount(0)
                    .duration(0)
                    .build();
        }
        
        try {
            log.info("开始批量刷新商品缓存，数量: {}", productIds.size());
            
            // 批量查询商品
            List<Product> products = productMapper.selectByIds(productIds);
            
            if (products == null || products.isEmpty()) {
                log.warn("未查询到商品数据");
                return CacheRefreshResult.builder()
                        .totalCount(productIds.size())
                        .successCount(0)
                        .failedCount(productIds.size())
                        .duration(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // 找出未查询到的ID
            List<Long> foundIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            List<Long> notFoundIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            
            // 批量写入缓存
            int successCount = 0;
            List<Long> failedIds = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();
            
            for (Product product : products) {
                try {
                    productDetailCache.put(product.getId(), product);
                    successCount++;
                } catch (Exception e) {
                    log.error("写入商品缓存失败，ID: {}", product.getId(), e);
                    failedIds.add(product.getId());
                    failedReasons.add("写入缓存失败: " + e.getMessage());
                }
            }
            
            // 添加未找到的ID到失败列表
            for (Long notFoundId : notFoundIds) {
                failedIds.add(notFoundId);
                failedReasons.add("商品不存在");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("商品缓存批量刷新完成，成功: {}, 失败: {}, 耗时: {}ms", 
                    successCount, failedIds.size(), duration);
            
            return CacheRefreshResult.builder()
                    .totalCount(productIds.size())
                    .successCount(successCount)
                    .failedCount(failedIds.size())
                    .failedIds(failedIds)
                    .failedReasons(failedReasons)
                    .duration(duration)
                    .build();
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("批量刷新商品缓存失败", e);
            
            return CacheRefreshResult.builder()
                    .totalCount(productIds.size())
                    .successCount(0)
                    .failedCount(productIds.size())
                    .duration(duration)
                    .build();
        }
    }
    
    /**
     * 批量刷新文章缓存
     * 根据文章ID列表从数据库查询并加载到缓存
     * 
     * @param articleIds 文章ID列表
     * @return 缓存刷新结果
     */
    public CacheRefreshResult refreshArticles(List<Long> articleIds) {
        long startTime = System.currentTimeMillis();
        
        if (articleIds == null || articleIds.isEmpty()) {
            log.warn("文章ID列表为空，跳过刷新");
            return CacheRefreshResult.builder()
                    .totalCount(0)
                    .successCount(0)
                    .failedCount(0)
                    .duration(0)
                    .build();
        }
        
        try {
            log.info("开始批量刷新文章缓存，数量: {}", articleIds.size());
            
            // 批量查询文章
            List<Article> articles = articleMapper.selectByIds(articleIds);
            
            if (articles == null || articles.isEmpty()) {
                log.warn("未查询到文章数据");
                return CacheRefreshResult.builder()
                        .totalCount(articleIds.size())
                        .successCount(0)
                        .failedCount(articleIds.size())
                        .duration(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            // 找出未查询到的ID
            List<Long> foundIds = articles.stream()
                    .map(Article::getId)
                    .collect(Collectors.toList());
            List<Long> notFoundIds = articleIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            
            // 批量写入缓存
            int successCount = 0;
            List<Long> failedIds = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();
            
            for (Article article : articles) {
                try {
                    articleDetailCache.put(article.getId(), article);
                    successCount++;
                } catch (Exception e) {
                    log.error("写入文章缓存失败，ID: {}", article.getId(), e);
                    failedIds.add(article.getId());
                    failedReasons.add("写入缓存失败: " + e.getMessage());
                }
            }
            
            // 添加未找到的ID到失败列表
            for (Long notFoundId : notFoundIds) {
                failedIds.add(notFoundId);
                failedReasons.add("文章不存在");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("文章缓存批量刷新完成，成功: {}, 失败: {}, 耗时: {}ms", 
                    successCount, failedIds.size(), duration);
            
            return CacheRefreshResult.builder()
                    .totalCount(articleIds.size())
                    .successCount(successCount)
                    .failedCount(failedIds.size())
                    .failedIds(failedIds)
                    .failedReasons(failedReasons)
                    .duration(duration)
                    .build();
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("批量刷新文章缓存失败", e);
            
            return CacheRefreshResult.builder()
                    .totalCount(articleIds.size())
                    .successCount(0)
                    .failedCount(articleIds.size())
                    .duration(duration)
                    .build();
        }
    }
    
    /**
     * 批量刷新景点缓存
     * 根据景点ID列表从数据库查询并加载到缓存
     * 
     * @param attractionIds 景点ID列表
     * @return 缓存刷新结果
     */
    public CacheRefreshResult refreshAttractions(List<Long> attractionIds) {
        long startTime = System.currentTimeMillis();
        
        if (attractionIds == null || attractionIds.isEmpty()) {
            log.warn("景点ID列表为空，跳过刷新");
            return CacheRefreshResult.builder()
                    .totalCount(0)
                    .successCount(0)
                    .failedCount(0)
                    .duration(0)
                    .build();
        }
        
        try {
            log.info("开始批量刷新景点缓存，数量: {}", attractionIds.size());
            
            // 逐个查询景点（AttractionMapper暂无批量查询方法）
            int successCount = 0;
            List<Long> failedIds = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();
            
            for (Long attractionId : attractionIds) {
                try {
                    Attraction attraction = attractionMapper.selectById(attractionId);
                    if (attraction != null) {
                        attractionDetailCache.put(attractionId, attraction);
                        successCount++;
                    } else {
                        failedIds.add(attractionId);
                        failedReasons.add("景点不存在");
                    }
                } catch (Exception e) {
                    log.error("查询或写入景点缓存失败，ID: {}", attractionId, e);
                    failedIds.add(attractionId);
                    failedReasons.add("查询或写入失败: " + e.getMessage());
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("景点缓存批量刷新完成，成功: {}, 失败: {}, 耗时: {}ms", 
                    successCount, failedIds.size(), duration);
            
            return CacheRefreshResult.builder()
                    .totalCount(attractionIds.size())
                    .successCount(successCount)
                    .failedCount(failedIds.size())
                    .failedIds(failedIds)
                    .failedReasons(failedReasons)
                    .duration(duration)
                    .build();
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("批量刷新景点缓存失败", e);
            
            return CacheRefreshResult.builder()
                    .totalCount(attractionIds.size())
                    .successCount(0)
                    .failedCount(attractionIds.size())
                    .duration(duration)
                    .build();
        }
    }
}
