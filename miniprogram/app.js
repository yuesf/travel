/**
 * 小程序入口文件
 */

const auth = require('./utils/auth');
const storage = require('./utils/storage');
const cartApi = require('./api/cart');

App({
  /**
   * 全局数据
   */
  globalData: {
    userInfo: null, // 用户信息
    cartCount: 0, // 购物车数量
    logoUrl: null, // Logo URL，null 表示使用默认值
    // mediaBaseUrl: 'http://192.168.1.100:8080', // 媒体资源基础URL（可选）
    // 如果小程序无法访问localhost，可以设置为本机IP地址
    // 例如：http://192.168.1.100:8080
    // 设置方法：在onLaunch中设置 this.globalData.mediaBaseUrl = 'http://192.168.1.100:8080'
  },

  /**
   * 小程序初始化
   */
  onLaunch() {
    console.log('小程序启动');

    // 初始化用户信息
    this.initUserInfo();

    // 初始化购物车数量
    this.initCartCount();

    // 初始化 Logo 配置
    this.initLogoConfig();

    // 检查更新
    this.checkUpdate();
  },

  /**
   * 小程序显示
   */
  onShow() {
    console.log('小程序显示');

    // 更新购物车数量
    this.updateCartCount();
  },

  /**
   * 小程序隐藏
   */
  onHide() {
    console.log('小程序隐藏');
  },

  /**
   * 小程序错误
   */
  onError(msg) {
    console.error('小程序错误:', msg);
  },

  /**
   * 初始化用户信息
   */
  initUserInfo() {
    const userInfo = auth.getUserInfo();
    if (userInfo) {
      this.globalData.userInfo = userInfo;
    }
  },

  /**
   * 设置用户信息
   */
  setUserInfo(userInfo) {
    this.globalData.userInfo = userInfo;
    auth.setUserInfo(userInfo);
  },

  /**
   * 清除用户信息
   */
  clearUserInfo() {
    this.globalData.userInfo = null;
    auth.removeUserInfo();
  },

  /**
   * 初始化购物车数量（云购物车方案：从后端实时查询）
   */
  async initCartCount() {
    // 检查登录状态
    if (!auth.isLoggedIn()) {
      this.globalData.cartCount = 0;
      storage.setCartCount(0);
      return;
    }
    
    try {
      // 从后端实时查询购物车列表，获取最新数量
      const cartList = await cartApi.getCartList();
      const count = cartList ? cartList.length : 0;
      this.globalData.cartCount = count;
      storage.setCartCount(count);
    } catch (error) {
      console.error('初始化购物车数量失败:', error);
      // 失败时使用本地存储的数量
      const count = storage.getCartCount();
      this.globalData.cartCount = count || 0;
    }
  },

  /**
   * 更新购物车数量（云购物车方案：从后端实时查询）
   */
  async updateCartCount() {
    // 检查登录状态
    if (!auth.isLoggedIn()) {
      this.globalData.cartCount = 0;
      storage.setCartCount(0);
      return;
    }
    
    try {
      // 从后端实时查询购物车列表，获取最新数量
      const cartList = await cartApi.getCartList();
      const count = cartList ? cartList.length : 0;
      this.globalData.cartCount = count;
      storage.setCartCount(count);
    } catch (error) {
      console.error('更新购物车数量失败:', error);
      // 失败时使用本地存储的数量
      const count = storage.getCartCount();
      this.globalData.cartCount = count || 0;
    }
  },

  /**
   * 设置购物车数量
   */
  setCartCount(count) {
    this.globalData.cartCount = count;
    storage.setCartCount(count);
  },

  /**
   * 增加购物车数量
   */
  increaseCartCount(count = 1) {
    const currentCount = this.globalData.cartCount || 0;
    this.setCartCount(currentCount + count);
  },

  /**
   * 减少购物车数量
   */
  decreaseCartCount(count = 1) {
    const currentCount = this.globalData.cartCount || 0;
    this.setCartCount(Math.max(0, currentCount - count));
  },

  /**
   * 初始化 Logo 配置
   */
  async initLogoConfig() {
    try {
      const configApi = require('./api/config');
      const { normalizeUrl } = require('./utils/url');
      
      // 先从本地存储获取缓存的 Logo URL
      const cachedLogoUrl = storage.getStorage('MINIPROGRAM_LOGO_URL', null, true);
      if (cachedLogoUrl) {
        // 规范化缓存的 URL
        const normalizedUrl = normalizeUrl(cachedLogoUrl);
        this.globalData.logoUrl = normalizedUrl;
        console.log('使用缓存的 Logo:', normalizedUrl);
      }
      
      // 从接口获取最新配置
      try {
        const response = await configApi.getLogoConfig();
        console.log('获取 Logo 配置响应:', response);
        const logoUrl = response && response.logoUrl ? response.logoUrl : null;
        
        // 规范化 Logo URL（处理 localhost 等问题）
        const normalizedLogoUrl = logoUrl ? normalizeUrl(logoUrl) : null;
        
        // 更新全局数据和缓存（保存原始 URL，使用时再规范化）
        this.globalData.logoUrl = normalizedLogoUrl;
        if (logoUrl && logoUrl !== '') {
          storage.setStorage('MINIPROGRAM_LOGO_URL', logoUrl, true);
          console.log('Logo 配置已更新并缓存:', logoUrl, '规范化后:', normalizedLogoUrl);
        } else {
          // 如果配置为 null，清除缓存，使用默认值
          storage.removeStorage('MINIPROGRAM_LOGO_URL', true);
          console.log('Logo 配置为空，使用默认值');
        }
      } catch (error) {
        console.error('获取 Logo 配置失败:', error);
        // 失败时使用缓存的配置或默认值
        if (!this.globalData.logoUrl || this.globalData.logoUrl === '') {
          this.globalData.logoUrl = null; // null 表示使用默认值
        }
      }
    } catch (error) {
      console.error('初始化 Logo 配置失败:', error);
      this.globalData.logoUrl = null; // 使用默认值
    }
  },

  /**
   * 检查小程序更新
   */
  checkUpdate() {
    if (wx.canIUse('getUpdateManager')) {
      const updateManager = wx.getUpdateManager();

      updateManager.onCheckForUpdate((res) => {
        if (res.hasUpdate) {
          console.log('发现新版本');
        }
      });

      updateManager.onUpdateReady(() => {
        wx.showModal({
          title: '更新提示',
          content: '新版本已经准备好，是否重启应用？',
          success: (res) => {
            if (res.confirm) {
              updateManager.applyUpdate();
            }
          },
        });
      });

      updateManager.onUpdateFailed(() => {
        wx.showModal({
          title: '更新失败',
          content: '新版本下载失败，请删除小程序重新打开',
          showCancel: false,
        });
      });
    }
  },
});
