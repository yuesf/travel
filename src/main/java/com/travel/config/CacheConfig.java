package com.travel.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.travel.entity.MiniProgramConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置
 * 
 * @author travel-platform
 */
@Configuration
public class CacheConfig {
    
    /**
     * Token黑名单缓存
     */
    @Bean("tokenBlacklistCache")
    public Cache<String, String> tokenBlacklistCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
    }
    
    /**
     * 短信验证码缓存
     */
    @Bean("smsCodeCache")
    public Cache<String, String> smsCodeCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 小程序配置缓存
     */
    @Bean("miniprogramConfigCache")
    public Cache<String, MiniProgramConfig> miniprogramConfigCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 首页数据缓存
     */
    @Bean("homeCache")
    public Cache<String, com.travel.dto.HomeResponse> homeCache() {
        return Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 景点详情缓存
     */
    @Bean("attractionDetailCache")
    public Cache<Long, com.travel.entity.Attraction> attractionDetailCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 商品详情缓存
     */
    @Bean("productDetailCache")
    public Cache<Long, Object> productDetailCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 文章详情缓存
     */
    @Bean("articleDetailCache")
    public Cache<Long, com.travel.entity.Article> articleDetailCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 文章阅读量防刷缓存（记录用户访问时间）
     */
    @Bean("articleViewCache")
    public Cache<String, Long> articleViewCache() {
        return Caffeine.newBuilder()
                .maximumSize(100000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 小程序Session缓存
     * Key: miniprogram:session:{session_id}
     * Value: SessionInfo对象
     */
    @Bean("miniprogramSessionCache")
    public Cache<String, com.travel.dto.SessionInfo> miniprogramSessionCache() {
        return Caffeine.newBuilder()
                .maximumSize(50000)
                .expireAfterWrite(30, TimeUnit.DAYS) // 30天，与微信session_key有效期同步
                .build();
    }
}
