/**
 * 景点列表页
 */

const categoryApi = require('../../api/category');
const constants = require('../../utils/constants');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    attractionList: [], // 景点列表
    loading: false, // 加载状态
    hasMore: true, // 是否还有更多数据
    page: 1, // 当前页码
    pageSize: constants.PAGINATION.DEFAULT_PAGE_SIZE, // 每页数量
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('景点列表页加载', options);
    
    // 设置页面标题
    wx.setNavigationBarTitle({
      title: '景点列表',
    });
    
    // 加载景点列表
    this.loadAttractionList(true);
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadAttractionList(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadAttractionList(false);
    }
  },

  /**
   * 加载景点列表
   * @param {boolean} refresh 是否刷新（重置页码）
   */
  async loadAttractionList(refresh = false) {
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
      
      console.log('景点列表查询参数:', params);
      
      // 调用景点列表API
      const response = await categoryApi.getAttractions(params);
      
      console.log('景点列表API响应:', response);
      
      // 处理数据
      const list = response.list || response.data || [];
      const total = response.total || list.length;
      
      // 格式化景点数据
      const formattedList = list.map(attraction => {
        // 取景点图片列表的第一个作为展示图片
        let image = '/static/images/default-product.png';
        if (attraction.images && Array.isArray(attraction.images) && attraction.images.length > 0) {
          image = attraction.images[0];
        } else if (attraction.image) {
          image = attraction.image;
        } else if (attraction.coverImage) {
          image = attraction.coverImage;
        }
        
        return {
          id: attraction.id,
          name: attraction.name,
          image: image,
          price: attraction.ticketPrice || attraction.price || 0,
          city: attraction.city || '',
          // 列表页不显示简介，只显示评级和地址
          rating: attraction.rating || '',
          address: attraction.address || attraction.location || '',
        };
      });
      
      const attractionList = refresh ? formattedList : [...this.data.attractionList, ...formattedList];
      const hasMore = attractionList.length < total;

      this.setData({
        attractionList,
        page: page + 1,
        hasMore,
        loading: false,
      });
    } catch (error) {
      console.error('加载景点列表失败:', error);
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
   * 点击景点卡片
   */
  onAttractionTap(e) {
    const { product } = e.detail;
    if (!product || !product.id) {
      return;
    }
    
    wx.navigateTo({
      url: `/pages/attraction/detail?id=${product.id}`,
      fail: (err) => {
        console.error('跳转景点详情失败:', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none',
        });
      },
    });
  },
});
