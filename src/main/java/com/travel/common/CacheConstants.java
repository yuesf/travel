package com.travel.common;

import lombok.Getter;

/**
 * 缓存常量类 - 集中管理所有缓存相关的常量定义
 * 
 * @author travel-platform
 */
public class CacheConstants {

    // ==================== 缓存Bean名称常量 ====================
    
    /**
     * Token黑名单缓存Bean名称
     */
    public static final String TOKEN_BLACKLIST_CACHE = "tokenBlacklistCache";
    
    /**
     * 短信验证码缓存Bean名称
     */
    public static final String SMS_CODE_CACHE = "smsCodeCache";
    
    /**
     * 小程序配置缓存Bean名称
     */
    public static final String MINIPROGRAM_CONFIG_CACHE = "miniprogramConfigCache";
    
    /**
     * 首页数据缓存Bean名称
     */
    public static final String HOME_CACHE = "homeCache";
    
    /**
     * 景点详情缓存Bean名称
     */
    public static final String ATTRACTION_DETAIL_CACHE = "attractionDetailCache";
    
    /**
     * 商品详情缓存Bean名称
     */
    public static final String PRODUCT_DETAIL_CACHE = "productDetailCache";
    
    /**
     * 文章详情缓存Bean名称
     */
    public static final String ARTICLE_DETAIL_CACHE = "articleDetailCache";
    
    /**
     * 文章阅读量防刷缓存Bean名称
     */
    public static final String ARTICLE_VIEW_CACHE = "articleViewCache";
    
    /**
     * 小程序Session缓存Bean名称
     */
    public static final String MINIPROGRAM_SESSION_CACHE = "miniprogramSessionCache";

    // ==================== Spring Cache注解使用的缓存名称 ====================
    
    /**
     * 支付配置缓存（Spring Cache）
     */
    public static final String PAYMENT_CONFIG_CACHE = "paymentConfig";

    // ==================== 缓存键前缀常量 ====================
    
    /**
     * 小程序Session缓存键前缀
     */
    public static final String MINIPROGRAM_SESSION_KEY_PREFIX = "miniprogram:session:";
    
    /**
     * 支付配置缓存键
     */
    public static final String PAYMENT_CONFIG_ENABLED_KEY = "enabled";
    
    /**
     * 首页数据缓存键
     */
    public static final String HOME_CACHE_KEY = "home:data";

    // ==================== 异步任务配置常量 ====================
    
    /**
     * 异步刷新阈值 - 超过此数量的数据刷新将使用异步模式
     */
    public static final int ASYNC_THRESHOLD = 100;
    
    /**
     * 异步任务线程池核心线程数
     */
    public static final int CORE_POOL_SIZE = 5;
    
    /**
     * 异步任务线程池最大线程数
     */
    public static final int MAX_POOL_SIZE = 10;
    
    /**
     * 异步任务线程池队列容量
     */
    public static final int QUEUE_CAPACITY = 100;
    
    /**
     * 异步任务线程池空闲线程存活时间（秒）
     */
    public static final int KEEP_ALIVE_SECONDS = 60;
    
    /**
     * 异步任务线程名称前缀
     */
    public static final String THREAD_NAME_PREFIX = "cache-refresh-";

    // ==================== 缓存类型枚举 ====================
    
    /**
     * 缓存类型枚举 - 定义所有支持的缓存类型
     */
    @Getter
    public enum CacheType {
        /**
         * Token黑名单缓存
         */
        TOKEN("token", "Token黑名单缓存", TOKEN_BLACKLIST_CACHE),
        
        /**
         * 短信验证码缓存
         */
        SMS("sms", "短信验证码缓存", SMS_CODE_CACHE),
        
        /**
         * 小程序配置缓存
         */
        MINIPROGRAM("miniprogram", "小程序配置缓存", MINIPROGRAM_CONFIG_CACHE),
        
        /**
         * 首页数据缓存
         */
        HOME("home", "首页数据缓存", HOME_CACHE),
        
        /**
         * 景点详情缓存
         */
        ATTRACTION("attraction", "景点详情缓存", ATTRACTION_DETAIL_CACHE),
        
        /**
         * 商品详情缓存
         */
        PRODUCT("product", "商品详情缓存", PRODUCT_DETAIL_CACHE),
        
        /**
         * 文章详情缓存
         */
        ARTICLE("article", "文章详情缓存", ARTICLE_DETAIL_CACHE),
        
        /**
         * 文章阅读量缓存
         */
        ARTICLE_VIEW("articleView", "文章阅读量缓存", ARTICLE_VIEW_CACHE),
        
        /**
         * 小程序Session缓存
         */
        MINIPROGRAM_SESSION("miniprogramSession", "小程序Session缓存", MINIPROGRAM_SESSION_CACHE),
        
        /**
         * 支付配置缓存
         */
        PAYMENT("payment", "支付配置缓存", PAYMENT_CONFIG_CACHE);

        /**
         * 缓存类型代码
         */
        private final String code;
        
        /**
         * 缓存类型描述
         */
        private final String description;
        
        /**
         * 对应的缓存Bean名称
         */
        private final String cacheName;

        CacheType(String code, String description, String cacheName) {
            this.code = code;
            this.description = description;
            this.cacheName = cacheName;
        }

        /**
         * 根据代码获取缓存类型
         * 
         * @param code 缓存类型代码
         * @return 缓存类型枚举
         */
        public static CacheType fromCode(String code) {
            if (code == null || code.trim().isEmpty()) {
                return null;
            }
            for (CacheType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * 检查是否为有效的缓存类型代码
         * 
         * @param code 缓存类型代码
         * @return true如果有效，false如果无效
         */
        public static boolean isValidCode(String code) {
            return fromCode(code) != null;
        }
    }
}
