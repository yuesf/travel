package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.MapCreateRequest;
import com.travel.dto.MapUpdateRequest;
import com.travel.entity.MapLocation;
import com.travel.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 地图管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController("adminMapController")
@RequestMapping("/api/v1/admin/maps")
@Tag(name = "地图管理")
public class MapController {
    
    @Autowired
    private MapService mapService;
    
    /**
     * 查询地图列表
     */
    @GetMapping
    @Operation(summary = "查询地图列表")
    public Result<List<MapLocation>> list(@RequestParam(required = false) Integer status) {
        List<MapLocation> result = mapService.list(status);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询地图详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询地图详情")
    public Result<MapLocation> getById(@PathVariable Long id) {
        MapLocation mapLocation = mapService.getById(id);
        return Result.success(mapLocation);
    }
    
    /**
     * 创建地图
     */
    @PostMapping
    @Operation(summary = "创建地图")
    public Result<MapLocation> create(@Valid @RequestBody MapCreateRequest request) {
        MapLocation mapLocation = mapService.create(request);
        return Result.success(mapLocation);
    }
    
    /**
     * 更新地图
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新地图")
    public Result<MapLocation> update(@PathVariable Long id, 
                              @Valid @RequestBody MapUpdateRequest request) {
        MapLocation mapLocation = mapService.update(id, request);
        return Result.success(mapLocation);
    }
    
    /**
     * 删除地图
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地图")
    public Result<?> delete(@PathVariable Long id) {
        mapService.delete(id);
        return Result.success();
    }
}
