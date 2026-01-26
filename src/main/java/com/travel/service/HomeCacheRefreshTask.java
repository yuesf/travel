package com.travel.service;

import com.travel.service.CacheRefreshResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 首页缓存刷新定时任务
 * 定时刷新首页缓存，重新生成签名URL，确保缓存中的签名URL始终有效
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class HomeCacheRefreshTask {
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * 定时刷新首页缓存
     * 每50分钟执行一次，确保在签名URL过期前更新（签名URL有效期为1小时）
     * 
     * Cron表达式:  表示每50分钟执行一次
     * 格式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 */50 * * * ?")
    @Async
    public void refreshHomeCache() {
        try {
            log.info("开始执行首页缓存刷新定时任务");
            long startTime = System.currentTimeMillis();
            
            // 调用 CacheService 刷新首页缓存
            // 该方法会重新从数据库查询数据，生成签名URL，并更新缓存
            CacheRefreshResult result = cacheService.refreshHome();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("首页缓存刷新定时任务完成 - 成功: {}, 失败: {}, 耗时: {}ms", 
                result.getSuccessCount(), result.getFailedCount(), duration);
                
        } catch (Exception e) {
            log.error("首页缓存刷新定时任务执行失败", e);
            // 不抛出异常，避免影响定时任务调度
        }
    }
}
