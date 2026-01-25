/**
 * 自定义导航栏组件
 */

const storage = require('../../utils/storage');
const { normalizeUrl } = require('../../utils/url');

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 导航栏标题
    title: {
      type: String,
      value: '标题'
    },
    // 背景颜色
    backgroundColor: {
      type: String,
      value: '#409EFF'
    }
  },

  /**
   * 组件的初始数据
   */
  data: {
    statusBarHeight: 0, // 状态栏高度
    navBarHeight: 44, // 导航栏高度（默认44px）
    logoUrl: '/assets/icons/logo.png', // 小程序 Logo，默认值
  },

  /**
   * 组件的方法列表
   */
  lifetimes: {
    attached() {
      // 初始化导航栏
      this.initNavBar();
      // 加载 Logo
      this.loadLogo();
    }
  },

  methods: {
    /**
     * 初始化导航栏
     */
    initNavBar() {
      try {
        const systemInfo = wx.getSystemInfoSync();
        const statusBarHeight = systemInfo.statusBarHeight || 0;
        
        // 获取胶囊按钮位置信息
        const menuButtonInfo = wx.getMenuButtonBoundingClientRect();
        
        // 计算导航栏高度：胶囊按钮的高度 + 胶囊按钮与状态栏的间距 * 2
        const navBarHeight = menuButtonInfo.height + (menuButtonInfo.top - statusBarHeight) * 2;
        
        this.setData({
          statusBarHeight: statusBarHeight,
          navBarHeight: navBarHeight
        });
        
        console.log('导航栏信息:', {
          statusBarHeight,
          navBarHeight,
          menuButtonInfo
        });
      } catch (error) {
        console.error('获取导航栏信息失败:', error);
        // 使用默认值
        this.setData({
          statusBarHeight: 20,
          navBarHeight: 44
        });
      }
    },

    /**
     * 加载 Logo
     */
    loadLogo() {
      const app = getApp();
      
      // 优先使用全局数据（需要明确检查不是 null 和 undefined）
      if (app.globalData.logoUrl !== null && app.globalData.logoUrl !== undefined && app.globalData.logoUrl !== '') {
        // 规范化 URL（处理 localhost 等问题）
        const normalizedUrl = normalizeUrl(app.globalData.logoUrl);
        console.log('导航栏使用全局数据的 Logo:', normalizedUrl);
        this.setData({
          logoUrl: normalizedUrl,
        });
        return;
      }
      
      // 其次使用缓存
      const cachedLogoUrl = storage.getStorage('MINIPROGRAM_LOGO_URL', null, true);
      if (cachedLogoUrl && cachedLogoUrl !== '') {
        // 规范化 URL（处理 localhost 等问题）
        const normalizedUrl = normalizeUrl(cachedLogoUrl);
        console.log('导航栏使用缓存的 Logo:', normalizedUrl);
        this.setData({
          logoUrl: normalizedUrl,
        });
        // 更新全局数据（保存规范化后的 URL）
        if (app && app.globalData) {
          app.globalData.logoUrl = normalizedUrl;
        }
        return;
      }
      
      // 最后使用默认值
      console.log('导航栏使用默认 Logo');
      this.setData({
        logoUrl: '/assets/icons/logo.png',
      });
    },

    /**
     * Logo 加载失败时使用默认 Logo
     */
    onLogoError(e) {
      const currentLogoUrl = this.data.logoUrl;
      
      // 如果当前不是默认 Logo，则使用默认 Logo
      if (currentLogoUrl !== '/assets/icons/logo.png') {
        console.log('导航栏 Logo 加载失败，切换到默认 Logo');
        
        // 清除无效的缓存（如果当前使用的是缓存的 URL）
        const app = getApp();
        const cachedLogoUrl = storage.getStorage('MINIPROGRAM_LOGO_URL', null, true);
        if (cachedLogoUrl && currentLogoUrl.includes(cachedLogoUrl)) {
          console.log('清除无效的 Logo 缓存');
          storage.removeStorage('MINIPROGRAM_LOGO_URL', true);
          if (app && app.globalData) {
            app.globalData.logoUrl = null;
          }
        }
        
        // 切换到默认 Logo
        this.setData({
          logoUrl: '/assets/icons/logo.png',
        });
      }
    },

    /**
     * 获取导航栏总高度（供父页面使用）
     */
    getNavBarHeight() {
      return this.data.statusBarHeight + this.data.navBarHeight;
    }
  }
});
