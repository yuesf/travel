package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.HotelCreateRequest;
import com.travel.dto.HotelListRequest;
import com.travel.dto.HotelRoomCreateRequest;
import com.travel.dto.HotelRoomUpdateRequest;
import com.travel.dto.HotelUpdateRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import com.travel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 酒店管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/hotels")
@Tag(name = "酒店管理")
public class HotelController {
    
    @Autowired
    private HotelService hotelService;
    
    /**
     * 分页查询酒店列表
     */
    @GetMapping
    @Operation(summary = "分页查询酒店列表")
    public Result<PageResult<Hotel>> list(HotelListRequest request) {
        PageResult<Hotel> result = hotelService.list(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询酒店详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询酒店详情")
    public Result<Hotel> getById(@PathVariable Long id) {
        Hotel hotel = hotelService.getById(id);
        return Result.success(hotel);
    }
    
    /**
     * 创建酒店
     */
    @PostMapping
    @Operation(summary = "创建酒店")
    public Result<Hotel> create(@Valid @RequestBody HotelCreateRequest request) {
        Hotel hotel = hotelService.create(request);
        return Result.success(hotel);
    }
    
    /**
     * 更新酒店
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新酒店")
    public Result<Hotel> update(@PathVariable Long id, 
                                @Valid @RequestBody HotelUpdateRequest request) {
        Hotel hotel = hotelService.update(id, request);
        return Result.success(hotel);
    }
    
    /**
     * 删除酒店（软删除，改为下架）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除酒店")
    public Result<?> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return Result.success();
    }
    
    /**
     * 根据酒店ID查询房型列表
     */
    @GetMapping("/{hotelId}/rooms")
    @Operation(summary = "查询酒店房型列表")
    public Result<List<HotelRoom>> listRooms(@PathVariable Long hotelId) {
        List<HotelRoom> rooms = hotelService.listRooms(hotelId);
        return Result.success(rooms);
    }
    
    /**
     * 根据ID查询房型详情
     */
    @GetMapping("/rooms/{id}")
    @Operation(summary = "查询房型详情")
    public Result<HotelRoom> getRoomById(@PathVariable Long id) {
        HotelRoom room = hotelService.getRoomById(id);
        return Result.success(room);
    }
    
    /**
     * 创建房型
     */
    @PostMapping("/rooms")
    @Operation(summary = "创建房型")
    public Result<HotelRoom> createRoom(@Valid @RequestBody HotelRoomCreateRequest request) {
        HotelRoom room = hotelService.createRoom(request);
        return Result.success(room);
    }
    
    /**
     * 更新房型
     */
    @PutMapping("/rooms/{id}")
    @Operation(summary = "更新房型")
    public Result<HotelRoom> updateRoom(@PathVariable Long id,
                                       @Valid @RequestBody HotelRoomUpdateRequest request) {
        HotelRoom room = hotelService.updateRoom(id, request);
        return Result.success(room);
    }
    
    /**
     * 删除房型
     */
    @DeleteMapping("/rooms/{id}")
    @Operation(summary = "删除房型")
    public Result<?> deleteRoom(@PathVariable Long id) {
        hotelService.deleteRoom(id);
        return Result.success();
    }
}
