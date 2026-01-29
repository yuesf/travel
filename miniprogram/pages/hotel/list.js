/**
 * 酒店列表页
 */

const categoryApi = require('../../api/category');
const constants = require('../../utils/constants');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    hotelList: [], // 酒店列表
    loading: false, // 加载状态
    hasMore: true, // 是否还有更多数据
    page: 1, // 当前页码
    pageSize: constants.PAGINATION.DEFAULT_PAGE_SIZE, // 每页数量
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('酒店列表页加载', options);
    
    // 设置页面标题
    wx.setNavigationBarTitle({
      title: '酒店列表',
    });
    
    // 加载酒店列表
    this.loadHotelList(true);
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadHotelList(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadHotelList(false);
    }
  },

  /**
   * 加载酒店列表
   * @param {boolean} refresh 是否刷新（重置页码）
   */
  async loadHotelList(refresh = false) {
    if (this.data.loading) {
      return;
    }

    const page = refresh ? 1 : this.data.page;
    
    this.setData({
      loading: true,
    });

    try {
      const params = {
        page,
        pageSize: this.data.pageSize,
      };
      
      console.log('酒店列表查询参数:', params);
      
      // 调用酒店列表API
      const response = await categoryApi.getHotels(params);
      
      console.log('酒店列表API响应:', response);
      
      // 处理数据
      const list = response.list || response.data || [];
      const total = response.total || list.length;
      
      // 格式化酒店数据
      const formattedList = list.map(hotel => {
        // 取酒店图片列表的第一个作为展示图片
        let image = '/static/images/default-product.png';
        if (hotel.images && Array.isArray(hotel.images) && hotel.images.length > 0) {
          image = hotel.images[0];
        } else if (hotel.image) {
          image = hotel.image;
        } else if (hotel.coverImage) {
          image = hotel.coverImage;
        }
        
        // 处理价格：确保价格字段正确
        let price = 0;
        if (hotel.price !== undefined && hotel.price !== null) {
          price = parseFloat(hotel.price) || 0;
        } else if (hotel.minPrice !== undefined && hotel.minPrice !== null) {
          price = parseFloat(hotel.minPrice) || 0;
        }
        
        return {
          id: hotel.id,
          name: hotel.name,
          image: image,
          price: price,
          minPrice: price, // 同时设置minPrice字段
          city: hotel.city || '',
          starLevel: hotel.starLevel || 0,
          // 列表页不显示简介，只显示设施和地址
          facilities: hotel.facilities || [],
          address: hotel.address || '',
        };
      });
      
      const hotelList = refresh ? formattedList : [...this.data.hotelList, ...formattedList];
      const hasMore = hotelList.length < total;

      this.setData({
        hotelList,
        page: page + 1,
        hasMore,
        loading: false,
      });
    } catch (error) {
      console.error('加载酒店列表失败:', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
      });
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 点击酒店卡片
   */
  onHotelTap(e) {
    const { product } = e.detail;
    if (!product || !product.id) {
      return;
    }
    
    wx.navigateTo({
      url: `/pages/hotel/detail?id=${product.id}`,
      fail: (err) => {
        console.error('跳转酒店详情失败:', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none',
        });
      },
    });
  },
});
