package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.HotelCreateRequest;
import com.travel.dto.HotelListRequest;
import com.travel.dto.HotelRoomCreateRequest;
import com.travel.dto.HotelRoomUpdateRequest;
import com.travel.dto.HotelUpdateRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import com.travel.exception.BusinessException;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.HotelRoomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 酒店服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class HotelService {
    
    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private HotelRoomMapper hotelRoomMapper;
    
    /**
     * 分页查询酒店列表
     */
    public PageResult<Hotel> list(HotelListRequest request) {
        // 参数校验
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getPageSize();
        
        // 查询列表
        List<Hotel> list = hotelMapper.selectList(
            request.getName(),
            request.getCity(),
            request.getStarLevel(),
            request.getStatus(),
            offset,
            request.getPageSize()
        );
        
        // 查询总数
        long total = hotelMapper.count(
            request.getName(),
            request.getCity(),
            request.getStarLevel(),
            request.getStatus()
        );
        
        return new PageResult<>(list, total, request.getPage(), request.getPageSize());
    }
    
    /**
     * 根据ID查询酒店详情
     */
    public Hotel getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        Hotel hotel = hotelMapper.selectById(id);
        if (hotel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return hotel;
    }
    
    /**
     * 创建酒店
     */
    @Transactional(rollbackFor = Exception.class)
    public Hotel create(HotelCreateRequest request) {
        // 创建酒店实体
        Hotel hotel = new Hotel();
        BeanUtils.copyProperties(request, hotel);
        
        // 设置默认状态
        if (hotel.getStatus() == null) {
            hotel.setStatus(1); // 默认上架
        }
        
        // 插入数据库
        int result = hotelMapper.insert(hotel);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建酒店成功: id={}, name={}", hotel.getId(), hotel.getName());
        
        return hotel;
    }
    
    /**
     * 更新酒店
     */
    @Transactional(rollbackFor = Exception.class)
    public Hotel update(Long id, HotelUpdateRequest request) {
        // 检查酒店是否存在
        Hotel hotel = getById(id);
        
        // 更新字段
        if (request.getName() != null) {
            hotel.setName(request.getName());
        }
        if (request.getAddress() != null) {
            hotel.setAddress(request.getAddress());
        }
        if (request.getProvince() != null) {
            hotel.setProvince(request.getProvince());
        }
        if (request.getCity() != null) {
            hotel.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            hotel.setDistrict(request.getDistrict());
        }
        if (request.getStarLevel() != null) {
            hotel.setStarLevel(request.getStarLevel());
        }
        if (request.getDescription() != null) {
            hotel.setDescription(request.getDescription());
        }
        if (request.getImages() != null) {
            hotel.setImages(request.getImages());
        }
        if (request.getFacilities() != null) {
            hotel.setFacilities(request.getFacilities());
        }
        if (request.getContactPhone() != null) {
            hotel.setContactPhone(request.getContactPhone());
        }
        if (request.getLongitude() != null) {
            hotel.setLongitude(request.getLongitude());
        }
        if (request.getLatitude() != null) {
            hotel.setLatitude(request.getLatitude());
        }
        if (request.getStatus() != null) {
            hotel.setStatus(request.getStatus());
        }
        
        // 更新数据库
        int result = hotelMapper.updateById(hotel);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新酒店成功: id={}, name={}", hotel.getId(), hotel.getName());
        
        return hotel;
    }
    
    /**
     * 删除酒店（软删除，改为下架）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查酒店是否存在
        Hotel hotel = getById(id);
        
        // 检查是否有关联订单
        long orderCount = hotelMapper.countOrdersByHotelId(id);
        if (orderCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "该酒店存在关联订单，无法删除，只能下架");
        }
        
        // 软删除：将状态改为下架
        HotelUpdateRequest updateRequest = new HotelUpdateRequest();
        updateRequest.setStatus(0);
        update(id, updateRequest);
        
        log.info("删除酒店成功（软删除）: id={}, name={}", hotel.getId(), hotel.getName());
    }
    
    /**
     * 根据酒店ID查询房型列表
     */
    public List<HotelRoom> listRooms(Long hotelId) {
        if (hotelId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        // 检查酒店是否存在
        getById(hotelId);
        
        return hotelRoomMapper.selectByHotelId(hotelId);
    }
    
    /**
     * 根据ID查询房型详情
     */
    public HotelRoom getRoomById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        HotelRoom room = hotelRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return room;
    }
    
    /**
     * 创建房型
     */
    @Transactional(rollbackFor = Exception.class)
    public HotelRoom createRoom(HotelRoomCreateRequest request) {
        // 检查酒店是否存在
        getById(request.getHotelId());
        
        // 创建房型实体
        HotelRoom room = new HotelRoom();
        BeanUtils.copyProperties(request, room);
        
        // 设置默认状态
        if (room.getStatus() == null) {
            room.setStatus(1); // 默认上架
        }
        
        // 插入数据库
        int result = hotelRoomMapper.insert(room);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建房型成功: id={}, hotelId={}, roomType={}", room.getId(), room.getHotelId(), room.getRoomType());
        
        return room;
    }
    
    /**
     * 更新房型
     */
    @Transactional(rollbackFor = Exception.class)
    public HotelRoom updateRoom(Long id, HotelRoomUpdateRequest request) {
        // 检查房型是否存在
        HotelRoom room = getRoomById(id);
        
        // 更新字段
        if (request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }
        if (request.getPrice() != null) {
            room.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            room.setStock(request.getStock());
        }
        if (request.getBedType() != null) {
            room.setBedType(request.getBedType());
        }
        if (request.getArea() != null) {
            room.setArea(request.getArea());
        }
        if (request.getFacilities() != null) {
            room.setFacilities(request.getFacilities());
        }
        if (request.getImages() != null) {
            room.setImages(request.getImages());
        }
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }
        
        // 更新数据库
        int result = hotelRoomMapper.updateById(room);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新房型成功: id={}, roomType={}", room.getId(), room.getRoomType());
        
        return room;
    }
    
    /**
     * 删除房型
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long id) {
        // 检查房型是否存在
        HotelRoom room = getRoomById(id);
        
        // 检查是否有关联订单
        long orderCount = hotelRoomMapper.countOrdersByRoomId(id);
        if (orderCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "该房型存在关联订单，无法删除");
        }
        
        // 物理删除
        int result = hotelRoomMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除房型成功: id={}, roomType={}", room.getId(), room.getRoomType());
    }
}
