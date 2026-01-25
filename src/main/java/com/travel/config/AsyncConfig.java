package com.travel.config;

import com.travel.common.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 为缓存刷新提供异步任务执行器支持
 * 
 * @author travel-platform
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 创建缓存刷新异步任务执行器
     * 
     * @return 线程池任务执行器
     */
    @Bean(name = "cacheRefreshExecutor")
    public ThreadPoolTaskExecutor cacheRefreshExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(CacheConstants.CORE_POOL_SIZE);
        
        // 最大线程数
        executor.setMaxPoolSize(CacheConstants.MAX_POOL_SIZE);
        
        // 队列容量
        executor.setQueueCapacity(CacheConstants.QUEUE_CAPACITY);
        
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(CacheConstants.KEEP_ALIVE_SECONDS);
        
        // 线程名称前缀
        executor.setThreadNamePrefix(CacheConstants.THREAD_NAME_PREFIX);
        
        // 拒绝策略：CallerRunsPolicy - 当线程池满时，由调用者线程执行任务（降级到同步执行）
        // 这样可以避免任务丢失，但会影响调用者的性能
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 优雅关闭：等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待终止时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("缓存刷新异步任务执行器已初始化 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
                CacheConstants.CORE_POOL_SIZE, 
                CacheConstants.MAX_POOL_SIZE, 
                CacheConstants.QUEUE_CAPACITY);
        
        return executor;
    }

    /**
     * 配置默认的异步任务执行器
     * 
     * @return 异步任务执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return cacheRefreshExecutor();
    }

    /**
     * 配置异步任务异常处理器
     * 
     * @return 异步未捕获异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * 自定义异步任务异常处理器
     * 记录异步任务执行过程中的异常信息
     */
    @Slf4j
    static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(@NonNull Throwable throwable, @NonNull Method method, @NonNull Object... params) {
            log.error("异步任务执行异常 - 方法: {}.{}, 参数: {}", 
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    params,
                    throwable);
            
            // 可以在这里添加其他处理逻辑，如：
            // - 发送告警通知
            // - 记录到数据库
            // - 更新任务状态为失败
        }
    }
}
