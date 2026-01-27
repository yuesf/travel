package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.AttractionBookingDateBatchRequest;
import com.travel.dto.AttractionBookingDateCreateRequest;
import com.travel.dto.AttractionBookingDateUpdateRequest;
import com.travel.entity.AttractionBookingDate;
import com.travel.service.AttractionBookingDateService;
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
 * 景点可订日期管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/attractions/{attractionId}/booking-dates")
@Tag(name = "景点可订日期管理")
public class AttractionBookingDateController {
    
    @Autowired
    private AttractionBookingDateService bookingDateService;
    
    /**
     * 查询可订日期列表
     */
    @GetMapping
    @Operation(summary = "查询可订日期列表")
    public Result<List<AttractionBookingDate>> list(
            @PathVariable Long attractionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttractionBookingDate> list = bookingDateService.list(attractionId, startDate, endDate);
        return Result.success(list);
    }
    
    /**
     * 根据ID查询可订日期详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询可订日期详情")
    public Result<AttractionBookingDate> getById(@PathVariable Long id) {
        AttractionBookingDate bookingDate = bookingDateService.getById(id);
        return Result.success(bookingDate);
    }
    
    /**
     * 创建可订日期
     */
    @PostMapping
    @Operation(summary = "创建可订日期")
    public Result<AttractionBookingDate> create(
            @PathVariable Long attractionId,
            @Valid @RequestBody AttractionBookingDateCreateRequest request) {
        request.setAttractionId(attractionId);
        AttractionBookingDate bookingDate = bookingDateService.create(request);
        return Result.success(bookingDate);
    }
    
    /**
     * 批量设置可订日期
     */
    @PostMapping("/batch")
    @Operation(summary = "批量设置可订日期")
    public Result<?> batchCreate(
            @PathVariable Long attractionId,
            @Valid @RequestBody AttractionBookingDateBatchRequest request) {
        request.setAttractionId(attractionId);
        bookingDateService.batchCreate(request);
        return Result.success();
    }
    
    /**
     * 更新可订日期
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新可订日期")
    public Result<AttractionBookingDate> update(
            @PathVariable Long id,
            @Valid @RequestBody AttractionBookingDateUpdateRequest request) {
        AttractionBookingDate bookingDate = bookingDateService.update(id, request);
        return Result.success(bookingDate);
    }
    
    /**
     * 删除可订日期
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除可订日期")
    public Result<?> delete(@PathVariable Long id) {
        bookingDateService.delete(id);
        return Result.success();
    }
}
