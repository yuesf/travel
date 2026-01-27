package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.AttractionGoldenSummitTimeSlotCreateRequest;
import com.travel.dto.AttractionGoldenSummitTimeSlotUpdateRequest;
import com.travel.entity.AttractionGoldenSummitTimeSlot;
import com.travel.service.AttractionGoldenSummitTimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 金顶时间段管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/attractions/{attractionId}/golden-summit-time-slots")
@Tag(name = "金顶时间段管理")
public class AttractionGoldenSummitTimeSlotController {
    
    @Autowired
    private AttractionGoldenSummitTimeSlotService timeSlotService;
    
    /**
     * 查询时间段列表
     */
    @GetMapping
    @Operation(summary = "查询时间段列表")
    public Result<List<AttractionGoldenSummitTimeSlot>> list(
            @PathVariable Long attractionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate) {
        List<AttractionGoldenSummitTimeSlot> list = timeSlotService.list(attractionId, bookingDate);
        return Result.success(list);
    }
    
    /**
     * 根据ID查询时间段详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询时间段详情")
    public Result<AttractionGoldenSummitTimeSlot> getById(@PathVariable Long id) {
        AttractionGoldenSummitTimeSlot timeSlot = timeSlotService.getById(id);
        return Result.success(timeSlot);
    }
    
    /**
     * 创建时间段
     */
    @PostMapping
    @Operation(summary = "创建时间段")
    public Result<AttractionGoldenSummitTimeSlot> create(
            @PathVariable Long attractionId,
            @Valid @RequestBody AttractionGoldenSummitTimeSlotCreateRequest request) {
        request.setAttractionId(attractionId);
        AttractionGoldenSummitTimeSlot timeSlot = timeSlotService.create(request);
        return Result.success(timeSlot);
    }
    
    /**
     * 更新时间段
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新时间段")
    public Result<AttractionGoldenSummitTimeSlot> update(
            @PathVariable Long id,
            @Valid @RequestBody AttractionGoldenSummitTimeSlotUpdateRequest request) {
        AttractionGoldenSummitTimeSlot timeSlot = timeSlotService.update(id, request);
        return Result.success(timeSlot);
    }
    
    /**
     * 删除时间段
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除时间段")
    public Result<?> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return Result.success();
    }
}
