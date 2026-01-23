/**
 * 登录页面
 * 仅支持微信授权登录
 */

const authApi = require('../../api/auth');
const auth = require('../../utils/auth');
const storage = require('../../utils/storage');
const { normalizeUrl } = require('../../utils/url');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    loading: false,
    logoUrl: '/assets/icons/logo.png', // 默认 Logo
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('登录页加载');
    this.loadLogo();
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
      console.log('使用全局数据的 Logo:', normalizedUrl);
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
      console.log('使用缓存的 Logo:', normalizedUrl);
      this.setData({
        logoUrl: normalizedUrl,
      });
      // 更新全局数据（保存规范化后的 URL）
      app.globalData.logoUrl = normalizedUrl;
      return;
    }
    
    // 最后使用默认值
    console.log('使用默认 Logo');
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
      console.log('Logo 加载失败，切换到默认 Logo');
      
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
   * 微信授权登录
   */
  async wxLogin() {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true });

    try {
      // 获取微信登录code
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({
          success: resolve,
          fail: reject,
        });
      });

      if (!loginRes.code) {
        wx.showToast({
          title: '获取登录凭证失败',
          icon: 'none',
        });
        this.setData({ loading: false });
        return;
      }

      // 调用后端登录接口（仅发送code）
      const response = await authApi.wechatLogin({
        code: loginRes.code,
      });

      // 打印完整响应数据，便于调试
      console.log('========== 登录响应 ==========');
      console.log('完整响应:', JSON.stringify(response, null, 2));
      console.log('Session ID:', response.sessionId);
      console.log('User ID:', response.userId);
      console.log('User Info:', response.userInfo);
      console.log('============================');

      // 保存Session ID
      if (response.sessionId) {
        auth.setSessionId(response.sessionId);
        console.log('✅ Session ID已保存:', response.sessionId);
      } else {
        console.error('❌ Session ID为空，登录失败');
        throw new Error('登录失败：未获取到Session ID');
      }

      // 保存用户信息
      // 优先使用 response.userInfo，如果没有则使用 response.userId 构建基本信息
      let userInfo = response.userInfo;
      if (!userInfo && response.userId) {
        // 如果 userInfo 不存在但 userId 存在，构建基本信息
        userInfo = {
          id: response.userId,
          nickname: response.username || '微信用户',
          avatar: null,
          phone: null,
          gender: null,
        };
        console.log('⚠️ userInfo为空，使用userId构建基本信息:', userInfo);
      } else if (userInfo) {
        // 如果 userInfo 存在，确保 id 和 nickname 有值
        if (!userInfo.id && response.userId) {
          userInfo.id = response.userId;
          console.log('⚠️ userInfo.id为空，补充userId:', response.userId);
        }
        // 如果昵称为空或只有空白字符，使用 username 或默认值
        if (!userInfo.nickname || userInfo.nickname.trim() === '') {
          userInfo.nickname = response.username || '微信用户';
          console.log('⚠️ userInfo.nickname为空，使用username:', userInfo.nickname);
        }
      }

      if (userInfo && userInfo.id) {
        auth.setUserInfo(userInfo);
        console.log('✅ 用户信息已保存:', JSON.stringify(userInfo, null, 2));
      } else {
        console.error('❌ 用户信息无效，无法保存');
        // 即使 userInfo 无效，也尝试获取用户信息
        // 注意：这里不立即调用 getUserInfo，因为 Session ID 可能还没有完全生效
        // 让目标页面在 onShow 时自己验证
        console.log('⚠️ 用户信息无效，将在目标页面重新获取');
      }

      const app = getApp();
      if (app && app.setUserInfo && userInfo) {
        app.setUserInfo(userInfo);
      }

      // 打印最终保存的用户信息
      console.log('========== 登录成功 ==========');
      console.log('Session ID:', response.sessionId);
      console.log('最终用户信息:', userInfo);
      console.log('用户ID:', userInfo?.id);
      console.log('用户昵称:', userInfo?.nickname);
      console.log('用户头像:', userInfo?.avatar);
      console.log('============================');

      wx.showToast({
        title: '登录成功',
        icon: 'success',
      });

      // 返回上一页或跳转到首页
      setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 1) {
          wx.navigateBack();
        } else {
          wx.switchTab({
            url: '/pages/mine/index',
          });
        }
      }, 1500);
    } catch (error) {
      console.error('微信登录失败:', error);
      
      // 检查是否是配置错误
      let errorMessage = error.message || '登录失败，请重试';
      const errorMessageLower = errorMessage.toLowerCase();
      
      // 如果是 AppID 配置错误，给出更友好的提示
      if (errorMessageLower.includes('invalid appid') || 
          errorMessageLower.includes('appid') && errorMessageLower.includes('invalid')) {
        errorMessage = '服务器配置错误：微信小程序 AppID 配置不正确，请联系管理员';
      } else if (errorMessageLower.includes('invalid secret') || 
                 errorMessageLower.includes('secret') && errorMessageLower.includes('invalid')) {
        errorMessage = '服务器配置错误：微信小程序 AppSecret 配置不正确，请联系管理员';
      } else if (errorMessageLower.includes('微信登录失败')) {
        // 其他微信登录错误，显示原始错误信息但更友好
        errorMessage = errorMessage.replace('微信登录失败: ', '');
      }
      
      wx.showToast({
        title: errorMessage,
        icon: 'none',
        duration: 3000,
      });
    } finally {
      this.setData({ loading: false });
    }
  },
});
