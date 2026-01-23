package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.entity.MapLocation;
import com.travel.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序地图控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/maps")
@Tag(name = "小程序地图")
public class MapController {
    
    @Autowired
    private MapService mapService;
    
    /**
     * 查询启用的地图列表
     */
    @GetMapping
    @Operation(summary = "查询启用的地图列表")
    public Result<List<MapLocation>> list() {
        // 只返回启用的地图（status = 1）
        List<MapLocation> result = mapService.list(1);
        return Result.success(result);
    }
}
