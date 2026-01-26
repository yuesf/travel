package com.travel.util;

import com.travel.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

/**
 * OSS URL处理工具类
 * 提供统一的OSS URL处理功能，支持单个URL、对象、列表的处理
 * 
 * 注意：OSS bucket已改为"私有写公有读"模式
 * - 写入操作（上传、删除）需要认证（私有写）
 * - 读取操作可以直接通过公开URL访问，无需签名（公有读）
 * 
 * 此工具类会将签名URL转换为公开URL（提取基础URL部分），
 * 对于已经是公开URL的情况，直接返回原URL。
 * 
 * @author travel-platform
 */
@Slf4j
@Component
public class OssUrlUtil {
    
    @Autowired
    private OssService ossService;
    
    /**
     * 默认签名URL有效期（秒）：1小时（已废弃，保留用于向后兼容）
     * 注意：OSS bucket已改为"私有写公有读"模式，不再需要生成签名URL
     */
    private static final int DEFAULT_EXPIRE_SECONDS = 3600;
    
    /**
     * OSS URL识别正则表达式模式
     */
    private static final Pattern[] OSS_URL_PATTERNS = {
        // 阿里云OSS：oss-xxx.aliyuncs.com 或 bucket.oss-region.aliyuncs.com
        Pattern.compile("oss-.*\\.aliyuncs\\.com", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\.oss-[^.]+\\.aliyuncs\\.com", Pattern.CASE_INSENSITIVE), // bucket.oss-cn-beijing.aliyuncs.com
        Pattern.compile("\\.oss\\.", Pattern.CASE_INSENSITIVE),
        // 腾讯云COS
        Pattern.compile("\\.qcloud\\.com", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\.cos\\.", Pattern.CASE_INSENSITIVE),
        // AWS S3
        Pattern.compile("\\.amazonaws\\.com", Pattern.CASE_INSENSITIVE)
    };
    
    /**
     * 处理单个URL，返回公开URL（使用默认有效期参数，已废弃但保留用于向后兼容）
     * 
     * 注意：OSS bucket已改为"私有写公有读"模式，此方法直接返回公开URL
     * 如果是签名URL，会提取基础URL部分（去掉查询参数）
     * 
     * @param url 原始URL（可能是签名URL或公开URL）
     * @return 公开URL，如果不是OSS URL或处理失败则返回原URL
     */
    public String processUrl(String url) {
        return processUrl(url, DEFAULT_EXPIRE_SECONDS);
    }
    
    /**
     * 处理单个URL，返回公开URL（expireSeconds参数已废弃，保留用于向后兼容）
     * 
     * 注意：OSS bucket已改为"私有写公有读"模式，此方法直接返回公开URL
     * 如果是签名URL，会提取基础URL部分（去掉查询参数）
     * 
     * @param url 原始URL（可能是签名URL或公开URL）
     * @param expireSeconds 过期时间（秒，已废弃，保留用于向后兼容）
     * @return 公开URL，如果不是OSS URL或处理失败则返回原URL
     */
    public String processUrl(String url, int expireSeconds) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // 检查OSS是否启用
        if (!ossService.isOssEnabled()) {
            log.debug("OSS未启用，返回原URL");
            return url;
        }
        
        // 判断是否为OSS URL
        if (!isOssUrl(url)) {
            log.debug("不是OSS URL，返回原URL: {}", url.length() > 50 ? url.substring(0, 50) + "..." : url);
            return url;
        }
        
        // OSS bucket已改为"私有写公有读"模式，直接返回公开URL
        // 如果是签名URL，提取基础URL部分（去掉查询参数）
        // 签名URL格式：https://bucket.endpoint/path?Expires=xxx&OSSAccessKeyId=xxx&Signature=xxx
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            // 检查是否包含签名参数（Expires= 或 Signature=）
            String queryString = url.substring(queryIndex + 1);
            if (queryString.contains("Expires=") || queryString.contains("Signature=")) {
                String publicUrl = url.substring(0, queryIndex);
                log.debug("从签名URL提取公开URL: {} -> {}", 
                    url.length() > 50 ? url.substring(0, 50) + "..." : url,
                    publicUrl.length() > 50 ? publicUrl.substring(0, 50) + "..." : publicUrl);
                return publicUrl;
            }
        }
        
        // 已经是公开URL，直接返回
        log.debug("URL已是公开URL，直接返回: {}", url.length() > 50 ? url.substring(0, 50) + "..." : url);
        return url;
    }
    
    /**
     * 处理对象中的URL字段（使用默认有效期参数，已废弃但保留用于向后兼容）
     * 
     * 注意：OSS bucket已改为"私有写公有读"模式，此方法会将签名URL转换为公开URL
     * 
     * @param obj 对象
     * @param urlFields URL字段名数组
     * @return 处理后的对象
     */
    public <T> T processObject(T obj, String[] urlFields) {
        return processObject(obj, urlFields, DEFAULT_EXPIRE_SECONDS);
    }
    
    /**
     * 处理对象中的URL字段（expireSeconds参数已废弃，保留用于向后兼容）
     * 
     * 注意：OSS bucket已改为"私有写公有读"模式，此方法会将签名URL转换为公开URL
     * 
     * @param obj 对象
     * @param urlFields URL字段名数组
     * @param expireSeconds 过期时间（秒，已废弃，保留用于向后兼容）
     * @return 处理后的对象
     */
    public <T> T processObject(T obj, String[] urlFields, int expireSeconds) {
        if (obj == null || urlFields == null || urlFields.length == 0) {
            return obj;
        }
        
        // 检查OSS是否启用
        if (!ossService.isOssEnabled()) {
            return obj;
        }
        
        try {
            Class<?> clazz = obj.getClass();
            
            // 处理每个URL字段
            for (String fieldName : urlFields) {
                try {
                    // 尝试通过getter方法获取字段值
                    String getterName = "get" + capitalize(fieldName);
                    Method getter = clazz.getMethod(getterName);
                    Object fieldValue = getter.invoke(obj);
                    
                    if (fieldValue == null) {
                        continue;
                    }
                    
                    // 如果是字符串，直接处理
                    if (fieldValue instanceof String) {
                        String url = (String) fieldValue;
                        if (!url.isEmpty()) {
                            String publicUrl = processUrl(url, expireSeconds);
                            if (!publicUrl.equals(url)) {
                                // 通过setter方法设置公开URL（从签名URL转换而来）
                                String setterName = "set" + capitalize(fieldName);
                                Method setter = clazz.getMethod(setterName, String.class);
                                setter.invoke(obj, publicUrl);
                            }
                        }
                    }
                    // 如果是列表，递归处理
                    else if (fieldValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) fieldValue;
                        processList(list, urlFields, expireSeconds);
                    }
                    // 如果是对象，递归处理
                    else if (fieldValue.getClass().getPackage() != null && 
                             fieldValue.getClass().getPackage().getName().startsWith("com.travel")) {
                        processObject(fieldValue, urlFields, expireSeconds);
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("字段 {} 没有getter/setter方法，跳过处理", fieldName);
                } catch (Exception e) {
                    log.warn("处理字段 {} 失败: {}", fieldName, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("处理对象URL失败", e);
        }
        
        return obj;
    }
    
    /**
     * 处理列表中的URL字段（使用默认有效期参数，已废弃但保留用于向后兼容）
     * 
     * 注意：OSS bucket已改为"私有写公有读"模式，此方法会将签名URL转换为公开URL
     * 
     * @param list 对象列表
     * @param urlFields URL字段名数组
     * @return 处理后的列表
     */
    public <T> List<T> processList(List<T> list, String[] urlFields) {
        return processList(list, urlFields, DEFAULT_EXPIRE_SECONDS);
    }
    
    /**
     * 处理列表中的URL字段（expireSeconds参数已废弃，保留用于向后兼容）
     * 
     * 注意：OSS bucket已改为"私有写公有读"模式，此方法会将签名URL转换为公开URL
     * 
     * @param list 对象列表
     * @param urlFields URL字段名数组
     * @param expireSeconds 过期时间（秒，已废弃，保留用于向后兼容）
     * @return 处理后的列表
     */
    public <T> List<T> processList(List<T> list, String[] urlFields, int expireSeconds) {
        if (list == null || list.isEmpty() || urlFields == null || urlFields.length == 0) {
            return list;
        }
        
        // 检查OSS是否启用
        if (!ossService.isOssEnabled()) {
            return list;
        }
        
        // 处理列表中的每个元素
        for (T item : list) {
            processObject(item, urlFields, expireSeconds);
        }
        
        return list;
    }
    
    /**
     * 判断是否为OSS URL
     * 
     * @param url URL字符串
     * @return true-是OSS URL，false-不是OSS URL
     */
    public boolean isOssUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        // 必须是HTTPS URL
        if (!url.startsWith("https://")) {
            return false;
        }
        
        // 不包含localhost（开发环境）
        if (url.contains("localhost")) {
            return false;
        }
        
        // 检查是否匹配OSS URL模式
        for (Pattern pattern : OSS_URL_PATTERNS) {
            if (pattern.matcher(url).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
