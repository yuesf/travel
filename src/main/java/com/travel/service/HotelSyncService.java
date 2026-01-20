package com.travel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.ResultCode;
import com.travel.dto.ExternalHotel;
import com.travel.dto.ExternalHotelRoom;
import com.travel.dto.HotelCreateRequest;
import com.travel.dto.HotelRoomCreateRequest;
import com.travel.dto.HotelUpdateRequest;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import com.travel.entity.SyncConfig;
import com.travel.entity.SyncLog;
import com.travel.exception.BusinessException;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.HotelRoomMapper;
import com.travel.mapper.SyncConfigMapper;
import com.travel.mapper.SyncLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 酒店同步服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class HotelSyncService {
    
    @Autowired
    private SyncConfigMapper syncConfigMapper;
    
    @Autowired
    private SyncLogMapper syncLogMapper;
    
    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private HotelRoomMapper hotelRoomMapper;
    
    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String SYNC_TYPE_HOTEL = "HOTEL";
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1秒
    
    /**
     * 手动同步酒店数据
     */
    @Transactional(rollbackFor = Exception.class)
    public SyncLog manualSync(Long configId) {
        SyncConfig config = syncConfigMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        if (!SYNC_TYPE_HOTEL.equals(config.getSyncType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "同步配置类型不是酒店类型");
        }
        
        return doSync(config);
    }
    
    /**
     * 定时自动同步酒店数据
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void autoSync() {
        log.info("开始执行定时自动同步酒店数据任务");
        
        // 查询所有启用的酒店同步配置
        List<SyncConfig> configs = syncConfigMapper.selectEnabled();
        for (SyncConfig config : configs) {
            if (!SYNC_TYPE_HOTEL.equals(config.getSyncType())) {
                continue;
            }
            
            // 检查同步频率
            if ("DAILY".equals(config.getSyncFrequency())) {
                // 每日同步，检查是否到了同步时间
                if (shouldSync(config)) {
                    try {
                        doSync(config);
                    } catch (Exception e) {
                        log.error("定时同步失败: configId={}, error={}", config.getId(), e.getMessage(), e);
                    }
                }
            } else if ("WEEKLY".equals(config.getSyncFrequency())) {
                // 每周同步，检查是否到了同步时间（每周一凌晨）
                if (shouldSyncWeekly(config)) {
                    try {
                        doSync(config);
                    } catch (Exception e) {
                        log.error("定时同步失败: configId={}, error={}", config.getId(), e.getMessage(), e);
                    }
                }
            }
        }
        
        log.info("定时自动同步酒店数据任务完成");
    }
    
    /**
     * 执行同步操作
     */
    private SyncLog doSync(SyncConfig config) {
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType(config.getSyncType());
        syncLog.setSyncConfigId(config.getId());
        syncLog.setStatus("FAILED");
        syncLog.setTotalCount(0);
        syncLog.setSuccessCount(0);
        syncLog.setFailedCount(0);
        syncLog.setStartTime(LocalDateTime.now());
        
        try {
            // 调用外部API获取数据
            List<ExternalHotel> externalHotels = fetchExternalData(config);
            
            syncLog.setTotalCount(externalHotels.size());
            
            // 同步数据
            int successCount = 0;
            int failedCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            for (ExternalHotel external : externalHotels) {
                try {
                    syncHotel(external, config);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    String errorMsg = String.format("同步酒店失败: name=%s, error=%s", 
                        external.getName(), e.getMessage());
                    errorMessages.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }
            
            syncLog.setSuccessCount(successCount);
            syncLog.setFailedCount(failedCount);
            syncLog.setStatus(successCount > 0 ? "SUCCESS" : "FAILED");
            
            if (!errorMessages.isEmpty()) {
                syncLog.setErrorMessage(String.join("; ", errorMessages));
            }
            
            // 更新最后同步时间
            syncConfigMapper.updateLastSyncTime(config.getId(), LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("同步失败: configId={}, error={}", config.getId(), e.getMessage(), e);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setStatus("FAILED");
        } finally {
            syncLog.setEndTime(LocalDateTime.now());
            syncLogMapper.insert(syncLog);
        }
        
        return syncLog;
    }
    
    /**
     * 调用外部API获取数据（带重试机制）
     */
    private List<ExternalHotel> fetchExternalData(SyncConfig config) {
        Exception lastException = null;
        
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            try {
                // 构建请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                
                if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                    headers.set("Authorization", "Bearer " + config.getApiKey());
                }
                
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                // 调用外部API
                ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.GET,
                    entity,
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // 解析JSON响应
                    ObjectMapper objectMapper = new ObjectMapper();
                    String body = response.getBody();
                    
                    // 尝试解析为数组
                    try {
                        List<ExternalHotel> hotels = objectMapper.readValue(
                            body,
                            new TypeReference<List<ExternalHotel>>() {}
                        );
                        return hotels;
                    } catch (Exception e) {
                        // 如果不是数组，尝试解析为对象包装的数组
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) objectMapper.readValue(body, Map.class);
                        if (map.containsKey("data")) {
                            Object data = map.get("data");
                            return objectMapper.convertValue(data, new TypeReference<List<ExternalHotel>>() {});
                        }
                        throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "外部API返回数据格式不正确");
                    }
                } else {
                    throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                        "外部API调用失败: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                lastException = e;
                log.warn("调用外部API失败，重试 {}/{}: error={}", i + 1, MAX_RETRY_COUNT, e.getMessage());
                
                if (i < MAX_RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * (i + 1)); // 递增延迟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "同步被中断");
                    }
                }
            }
        }
        
        throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
            "调用外部API失败，已重试" + MAX_RETRY_COUNT + "次: " + 
            (lastException != null ? lastException.getMessage() : "未知错误"));
    }
    
    /**
     * 同步单个酒店（增量同步，根据名称+地址判断是否已存在）
     */
    private void syncHotel(ExternalHotel external, SyncConfig config) {
        // 验证数据
        if (external.getName() == null || external.getName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "酒店名称不能为空");
        }
        
        // 根据名称+地址查询是否已存在
        List<Hotel> existing = hotelMapper.selectList(
            external.getName(),
            external.getCity(),
            null,
            null,
            0,
            100
        );
        
        Hotel hotel = null;
        for (Hotel h : existing) {
            // 判断名称和地址是否匹配
            if (external.getName().equals(h.getName()) && 
                (external.getAddress() == null || external.getAddress().equals(h.getAddress()))) {
                hotel = h;
                break;
            }
        }
        
        if (hotel == null) {
            // 创建新酒店
            HotelCreateRequest createRequest = new HotelCreateRequest();
            BeanUtils.copyProperties(external, createRequest);
            createRequest.setStatus(1); // 默认上架
            hotel = hotelService.create(createRequest);
            log.info("同步创建酒店: name={}", external.getName());
        } else {
            // 更新已有酒店
            HotelUpdateRequest updateRequest = new HotelUpdateRequest();
            BeanUtils.copyProperties(external, updateRequest);
            hotel = hotelService.update(hotel.getId(), updateRequest);
            log.info("同步更新酒店: id={}, name={}", hotel.getId(), external.getName());
        }
        
        // 同步房型信息
        if (external.getRooms() != null && !external.getRooms().isEmpty()) {
            syncHotelRooms(hotel.getId(), external.getRooms());
        }
    }
    
    /**
     * 同步酒店房型
     */
    private void syncHotelRooms(Long hotelId, List<ExternalHotelRoom> externalRooms) {
        // 获取现有房型列表
        List<HotelRoom> existingRooms = hotelRoomMapper.selectByHotelId(hotelId);
        
        for (ExternalHotelRoom externalRoom : externalRooms) {
            try {
                // 查找是否已存在相同房型
                HotelRoom existingRoom = null;
                for (HotelRoom room : existingRooms) {
                    if (externalRoom.getRoomType() != null && 
                        externalRoom.getRoomType().equals(room.getRoomType())) {
                        existingRoom = room;
                        break;
                    }
                }
                
                if (existingRoom == null) {
                    // 创建新房型
                    HotelRoomCreateRequest createRequest = new HotelRoomCreateRequest();
                    BeanUtils.copyProperties(externalRoom, createRequest);
                    createRequest.setHotelId(hotelId);
                    createRequest.setStatus(1); // 默认上架
                    hotelService.createRoom(createRequest);
                    log.info("同步创建房型: hotelId={}, roomType={}", hotelId, externalRoom.getRoomType());
                } else {
                    // 更新已有房型
                    com.travel.dto.HotelRoomUpdateRequest updateRequest = new com.travel.dto.HotelRoomUpdateRequest();
                    BeanUtils.copyProperties(externalRoom, updateRequest);
                    hotelService.updateRoom(existingRoom.getId(), updateRequest);
                    log.info("同步更新房型: id={}, roomType={}", existingRoom.getId(), externalRoom.getRoomType());
                }
            } catch (Exception e) {
                log.error("同步房型失败: hotelId={}, roomType={}, error={}", 
                    hotelId, externalRoom.getRoomType(), e.getMessage(), e);
                // 继续同步其他房型，不中断整个同步过程
            }
        }
    }
    
    /**
     * 判断是否应该同步（每日同步）
     */
    private boolean shouldSync(SyncConfig config) {
        if (config.getSyncTime() == null || config.getSyncTime().isEmpty()) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        String[] timeParts = config.getSyncTime().split(":");
        if (timeParts.length != 2) {
            return true;
        }
        
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        return now.getHour() == hour && now.getMinute() == minute;
    }
    
    /**
     * 判断是否应该同步（每周同步）
     */
    private boolean shouldSyncWeekly(SyncConfig config) {
        LocalDateTime now = LocalDateTime.now();
        // 每周一凌晨执行
        return now.getDayOfWeek().getValue() == 1 && shouldSync(config);
    }
}
