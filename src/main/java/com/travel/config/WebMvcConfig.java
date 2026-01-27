package com.travel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Web MVC配置
 * 
 * @author travel-platform
 */
@Configuration
public class WebMvcConfig {
    // 静态资源访问配置已移至 FileUploadConfig
    
    /**
     * 配置Jackson ObjectMapper，支持日期和日期时间格式
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // 定义日期格式：支持 "yyyy-MM-dd" 格式
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 定义日期时间格式：支持 "yyyy-MM-dd HH:mm:ss" 格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // 创建自定义模块
        SimpleModule module = new SimpleModule();
        // LocalDate 序列化和反序列化
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        module.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        // LocalDateTime 序列化和反序列化
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        
        // 构建ObjectMapper，启用JSR310模块支持
        return builder
                .modules(module)
                .build();
    }
}
