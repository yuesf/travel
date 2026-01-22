/**
 * H5 链接 web-view 页面
 */

Page({
  /**
   * 页面的初始数据
   */
  data: {
    url: '', // H5 链接地址
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('web-view 页面加载', options);
    
    const url = options.url ? decodeURIComponent(options.url) : '';
    
    // 验证 URL 格式
    if (!url || (!url.startsWith('http://') && !url.startsWith('https://'))) {
      wx.showToast({
        title: '链接地址无效',
        icon: 'none',
        duration: 2000,
      });
      setTimeout(() => {
        wx.navigateBack({
          fail: () => {
            wx.switchTab({
              url: '/pages/home/index',
            });
          },
        });
      }, 2000);
      return;
    }
    
    this.setData({
      url: url,
    });
    
    // 设置导航栏标题
    wx.setNavigationBarTitle({
      title: '加载中...',
    });
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 页面显示时的处理
  },

  /**
   * web-view 加载完成
   */
  onWebViewLoad() {
    console.log('web-view 加载完成');
    wx.setNavigationBarTitle({
      title: '网页',
    });
  },

  /**
   * web-view 加载错误
   */
  onWebViewError(e) {
    console.error('web-view 加载错误:', e);
    wx.showModal({
      title: '提示',
      content: '网页加载失败，请检查网络连接或链接地址是否正确',
      showCancel: false,
      success: () => {
        wx.navigateBack({
          fail: () => {
            wx.switchTab({
              url: '/pages/home/index',
            });
          },
        });
      },
    });
  },
});
