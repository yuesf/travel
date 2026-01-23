/**
 * 我的页面
 */

const auth = require('../../utils/auth');
const authApi = require('../../api/auth');
const orderApi = require('../../api/order');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    userInfo: null, // 用户信息
    isLoggedIn: false, // 是否已登录
    loading: false, // 加载状态
    orderCounts: {
      pendingPay: 0, // 待支付
      pendingUse: 0, // 待使用
      completed: 0, // 已完成
      cancelled: 0, // 已取消
    },
  },

  /**
   * 验证状态标记，避免频繁验证
   */
  _checkingStatus: false, // 是否正在验证登录状态
  _lastCheckTime: 0, // 上次验证时间
  _checkInterval: 5000, // 验证间隔（5秒）

  /**
   * 刷新状态标记，防止频繁刷新
   */
  _refreshing: false, // 是否正在刷新

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('我的页面加载');
    // 首次加载时重置验证时间，确保立即验证
    this._lastCheckTime = 0;
    this.setData({
      loading: true,
    });
    this.checkLoginStatus();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 页面显示时检查登录状态
    // 延迟一小段时间再检查，避免登录成功后立即跳转导致的时序问题
    setTimeout(() => {
      this.checkLoginStatus();
      if (this.data.isLoggedIn) {
        this.loadOrderCounts();
      }
    }, 100);
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  async onPullDownRefresh() {
    // 防止重复刷新
    if (this._refreshing) {
      wx.stopPullDownRefresh();
      return;
    }

    this._refreshing = true;

    try {
      // 刷新数据
      await this.refreshData();
    } catch (error) {
      console.error('下拉刷新失败:', error);
      // 刷新失败时，不显示错误提示，保持原有数据显示
      // 用户可以通过再次下拉重试
    } finally {
      // 无论成功还是失败，都要停止刷新动画
      this._refreshing = false;
      wx.stopPullDownRefresh();
    }
  },

  /**
   * 检查登录状态
   */
  async checkLoginStatus() {
    const hasSessionId = auth.isLoggedIn();
    const userInfo = auth.getUserInfo();
    
    console.log('========== 检查登录状态 ==========');
    console.log('Session ID是否存在:', hasSessionId);
    console.log('Session ID值:', auth.getSessionId());
    console.log('本地用户信息:', userInfo);
    console.log('用户ID:', userInfo?.id);
    console.log('============================');
    
    // 如果没有 sessionId，直接设置为未登录
    if (!hasSessionId) {
      this.setData({
        isLoggedIn: false,
        userInfo: null,
        loading: false,
      });
      return;
    }

    // 如果有 sessionId 和用户信息，先设置为已登录
    if (userInfo && userInfo.id) {
      this.setData({
        isLoggedIn: true,
        userInfo,
      });
    } else {
      // 有 sessionId 但没有用户信息，需要获取
      this.setData({
        isLoggedIn: true,
        userInfo: null,
      });
    }

    // 检查是否需要验证（避免频繁验证）
    // 如果本地有完整的用户信息，且距离上次验证时间太短，跳过验证
    const now = Date.now();
    if (this._checkingStatus) {
      console.log('跳过验证（正在验证中）');
      return;
    }
    
    // 如果本地有完整的用户信息，且距离上次验证时间太短，跳过验证
    if (userInfo && userInfo.id && now - this._lastCheckTime < this._checkInterval) {
      console.log('跳过验证（距离上次验证时间太短，且本地有用户信息）');
      return;
    }

    // 主动验证 sessionId 是否有效
    this._checkingStatus = true;
    this._lastCheckTime = now;
    
    // 再次确认 Session ID 存在（防止在检查后、请求前被清除）
    const currentSessionId = auth.getSessionId();
    if (!currentSessionId || typeof currentSessionId !== 'string' || currentSessionId.trim().length === 0) {
      console.log('❌ Session ID 不存在或无效，清除登录状态');
      auth.logout();
      const app = getApp();
      if (app && app.clearUserInfo) {
        app.clearUserInfo();
      }
      this.setData({
        isLoggedIn: false,
        userInfo: null,
      });
      this._checkingStatus = false;
      return;
    }
    
    try {
      console.log('验证 Session ID 有效性...');
      console.log('当前 Session ID:', currentSessionId);
      const validUserInfo = await authApi.getUserInfo({
        showLoading: false, // 不显示加载提示
        showError: false, // 不显示错误提示，我们自己处理
      });
      
      if (validUserInfo && validUserInfo.id) {
        // Session 有效，更新用户信息
        auth.setUserInfo(validUserInfo);
        const app = getApp();
        if (app && app.setUserInfo) {
          app.setUserInfo(validUserInfo);
        }
        this.setData({
          isLoggedIn: true,
          userInfo: validUserInfo,
        });
        console.log('✅ Session ID 有效，用户信息已更新');
      } else {
        // Session 无效，清除登录状态
        console.log('❌ Session ID 无效，清除登录状态');
        auth.logout();
        const app = getApp();
        if (app && app.clearUserInfo) {
          app.clearUserInfo();
        }
        this.setData({
          isLoggedIn: false,
          userInfo: null,
        });
      }
    } catch (error) {
      console.error('❌ 验证 Session ID 失败:', error);
      // 如果是401错误，说明session已过期，清除登录状态
      if (error.message && (error.message.includes('401') || error.message.includes('未授权'))) {
        console.log('❌ Session ID 已过期，清除登录状态');
        auth.logout();
        const app = getApp();
        if (app && app.clearUserInfo) {
          app.clearUserInfo();
        }
        this.setData({
          isLoggedIn: false,
          userInfo: null,
          loading: false,
        });
      } else if (userInfo && userInfo.id) {
        // 网络错误等其他错误，如果本地有用户信息，保持登录状态
        console.log('⚠️ 验证失败但保留本地登录状态（可能是网络问题）');
        this.setData({
          isLoggedIn: true,
          userInfo,
          loading: false,
        });
      } else {
        // 没有本地用户信息，设置为未登录
        this.setData({
          isLoggedIn: false,
          userInfo: null,
          loading: false,
        });
      }
    } finally {
      this._checkingStatus = false;
    }
  },

  /**
   * 刷新页面数据
   * 同时刷新用户信息和订单统计数据
   */
  async refreshData() {
    // 先检查登录状态
    await this.checkLoginStatus();

    // 如果已登录，刷新用户信息和订单统计
    if (this.data.isLoggedIn) {
      // 并行执行，提高效率
      await Promise.all([
        this.loadUserInfo(),
        this.loadOrderCounts(),
      ]);
    }
  },

  /**
   * 加载用户信息
   * @param {Object} options 请求选项（可选）
   * @param {boolean} options.showLoading 是否显示加载提示
   * @param {boolean} options.showError 是否显示错误提示
   */
  async loadUserInfo(options = {}) {
    try {
      console.log('开始获取用户信息...');
      const userInfo = await authApi.getUserInfo(options);
      console.log('获取到的用户信息:', JSON.stringify(userInfo, null, 2));
      
      if (userInfo && userInfo.id) {
        auth.setUserInfo(userInfo);
        const app = getApp();
        if (app && app.setUserInfo) {
          app.setUserInfo(userInfo);
        }
        this.setData({
          userInfo,
          isLoggedIn: true,
        });
        console.log('✅ 用户信息加载成功');
      } else {
        console.error('❌ 获取到的用户信息无效:', userInfo);
        // 如果获取到的用户信息无效，清除登录状态
        auth.logout();
        const app = getApp();
        if (app && app.clearUserInfo) {
          app.clearUserInfo();
        }
        this.setData({
          isLoggedIn: false,
          userInfo: null,
        });
      }
    } catch (error) {
      console.error('❌ 获取用户信息失败:', error);
      // 如果是401错误，说明session已过期，清除登录状态
      if (error.message && (error.message.includes('401') || error.message.includes('未授权'))) {
        auth.logout();
        const app = getApp();
        if (app && app.clearUserInfo) {
          app.clearUserInfo();
        }
        this.setData({
          isLoggedIn: false,
          userInfo: null,
        });
      }
    }
  },

  /**
   * 加载订单数量统计
   */
  async loadOrderCounts() {
    // 检查登录状态
    if (!auth.isLoggedIn()) {
      return;
    }

    try {
      const result = await orderApi.getOrderStatistics({
        showLoading: false, // 不显示加载提示
        showError: false, // 不显示错误提示，我们自己处理
      });
      console.log('订单统计:', result);
      
      // 处理返回的数据格式
      const counts = result || {};
      
      this.setData({
        orderCounts: {
          pendingPay: counts.pendingPay || 0,
          pendingUse: counts.pendingUse || 0,
          completed: counts.completed || 0,
          cancelled: counts.cancelled || 0,
        },
      });
    } catch (error) {
      console.error('加载订单统计失败:', error);
      // 如果是401错误，说明未授权，清除登录状态
      if (error.message && (error.message.includes('401') || error.message.includes('未授权') || error.message.includes('请先登录'))) {
        console.log('订单统计接口返回401，清除登录状态');
        auth.logout();
        const app = getApp();
        if (app && app.clearUserInfo) {
          app.clearUserInfo();
        }
        this.setData({
          isLoggedIn: false,
          userInfo: null,
        });
      }
      // 如果接口调用失败，保持当前数据不变（不重置为0）
    }
  },

  /**
   * 跳转到登录页
   */
  goToLogin() {
    wx.navigateTo({
      url: '/pages/mine/login',
    });
  },

  /**
   * 退出登录
   */
  async logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            // 调用后端登出接口
            await authApi.logout();
          } catch (error) {
            console.error('登出失败:', error);
          }
          
          // 清除本地数据
          auth.logout();
          const app = getApp();
          if (app && app.clearUserInfo) {
            app.clearUserInfo();
          }
          
          this.setData({
            isLoggedIn: false,
            userInfo: null,
            orderCounts: {
              pendingPay: 0,
              pendingUse: 0,
              completed: 0,
              cancelled: 0,
            },
          });
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success',
          });
        }
      },
    });
  },

  /**
   * 检查登录状态并执行回调
   * @param {Function} callback 登录成功后的回调函数
   */
  async checkAuthAndExecute(callback) {
    // 先检查本地登录状态
    const localIsLoggedIn = auth.isLoggedIn();
    const localUserInfo = auth.getUserInfo();
    
    if (!localIsLoggedIn || !localUserInfo || !localUserInfo.id) {
      console.log('未登录，跳转到登录页');
      this.goToLogin();
      return;
    }
    
    // 如果本地有登录信息，直接执行回调
    // 如果 sessionId 已过期，目标页面的 API 调用会失败，request.js 会处理
    if (callback) {
      callback();
    }
  },

  /**
   * 跳转到订单列表
   * @param {Object|string} e 事件对象或状态字符串
   */
  goToOrders(e) {
    // 如果传入的是事件对象，从 dataset 中获取 status
    // 如果传入的是字符串，直接使用
    // 如果都没有，默认为空字符串（全部订单）
    let status = '';
    if (e && typeof e === 'object' && e.currentTarget) {
      // 事件对象，从 dataset 中获取
      status = e.currentTarget.dataset.status || '';
    } else if (typeof e === 'string') {
      // 字符串参数
      status = e;
    }
    
    this.checkAuthAndExecute(() => {
      wx.navigateTo({
        url: `/pages/order/index?status=${status}`,
      });
    });
  },

  /**
   * 跳转到优惠券列表
   */
  goToCoupons() {
    this.checkAuthAndExecute(() => {
      wx.navigateTo({
        url: '/pages/coupon/index',
      });
    });
  },

  /**
   * 跳转到收藏列表
   */
  goToFavorites() {
    this.checkAuthAndExecute(() => {
      wx.navigateTo({
        url: '/pages/favorite/index',
      });
    });
  },

  /**
   * 跳转到收货地址管理
   */
  goToAddress() {
    this.checkAuthAndExecute(() => {
      wx.navigateTo({
        url: '/pages/mine/address',
      });
    });
  },

  /**
   * 跳转到客服中心
   */
  goToService() {
    wx.showToast({
      title: '客服功能开发中',
      icon: 'none',
    });
  },

  /**
   * 跳转到关于我们
   */
  goToAbout() {
    wx.showToast({
      title: '关于我们功能开发中',
      icon: 'none',
    });
  },
});
