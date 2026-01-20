package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.dto.HomeResponse;
import com.travel.entity.Article;
import com.travel.entity.Attraction;
import com.travel.entity.MiniProgramConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
}
