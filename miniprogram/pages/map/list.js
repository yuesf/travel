/**
 * 地图列表页
 */

const mapApi = require('../../api/map');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    mapList: [], // 地图列表
    loading: false, // 加载状态
    targetMapId: null, // 目标地图ID（从Icon传入，用于自动定位）
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('地图列表页加载', options);
    
    // 从页面参数获取目标地图ID
    if (options.mapId) {
      this.setData({
        targetMapId: Number(options.mapId),
      });
    }
    
    // 加载地图列表
    this.loadMapList();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 刷新地图列表
    this.loadMapList();
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadMapList().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 加载地图列表
   */
  async loadMapList() {
    if (this.data.loading) {
      return;
    }

    this.setData({
      loading: true,
    });

    try {
      const result = await mapApi.getMapList();
      console.log('地图列表:', result);

      // 处理返回的数据格式（request.get 已经返回了 data 字段的内容）
      const list = Array.isArray(result) ? result : (result?.data || []);

      // 为每个地图添加 markers 配置
      const mapList = list.map(map => ({
        ...map,
        markers: [{
          id: map.id,
          longitude: map.longitude,
          latitude: map.latitude,
          width: 30,
          height: 30,
          callout: {
            content: map.name,
            color: '#333',
            fontSize: 14,
            borderRadius: 4,
            bgColor: '#fff',
            padding: 8,
            display: 'ALWAYS'
          }
        }]
      }));

      this.setData({
        mapList: mapList,
        loading: false,
      });

      // 如果有目标地图ID，自动滚动到该地图
      if (this.data.targetMapId) {
        this.scrollToTargetMap();
      }
    } catch (error) {
      console.error('加载地图列表失败:', error);
      wx.showToast({
        title: '加载地图列表失败',
        icon: 'none',
      });
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 滚动到目标地图
   */
  scrollToTargetMap() {
    if (!this.data.targetMapId) {
      return;
    }

    // 延迟执行，确保页面已渲染
    setTimeout(() => {
      const query = wx.createSelectorQuery();
      query.select(`#map-${this.data.targetMapId}`).boundingClientRect();
      query.selectViewport().scrollOffset();
      query.exec((res) => {
        if (res[0]) {
          wx.pageScrollTo({
            scrollTop: res[1].scrollTop + res[0].top - 100, // 减去100rpx的偏移量
            duration: 300,
          });
        }
      });
    }, 500);
  },

  /**
   * 导航到指定地点
   */
  onNavigate(e) {
    const { longitude, latitude, name, address } = e.currentTarget.dataset;
    
    if (!longitude || !latitude) {
      wx.showToast({
        title: '位置信息不完整',
        icon: 'none',
      });
      return;
    }

    wx.openLocation({
      latitude: parseFloat(latitude),
      longitude: parseFloat(longitude),
      name: name || '目的地',
      address: address || '',
      scale: 18,
      success: () => {
        console.log('打开导航成功');
      },
      fail: (err) => {
        console.error('打开导航失败:', err);
        wx.showToast({
          title: '导航功能暂不可用',
          icon: 'none',
        });
      },
    });
  },
});
