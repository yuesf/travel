package com.travel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 旅游平台主启动类
 * 
 * @author travel-platform
 * @date 2024-01-15
 */
@SpringBootApplication
@MapperScan("com.travel.mapper")
@EnableAsync
@EnableScheduling
@EnableCaching
public class TravelPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelPlatformApplication.class, args);
    }
}
