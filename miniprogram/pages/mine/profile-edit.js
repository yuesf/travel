/**
 * 用户信息编辑页
 */

const auth = require('../../utils/auth');
const userApi = require('../../api/user');
const authApi = require('../../api/auth');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    nickname: '', // 昵称
    phone: '', // 手机号
    fromBooking: false, // 是否从预订页面跳转过来
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 检查是否从预订页面跳转过来
    if (options.fromBooking === 'true') {
      this.setData({
        fromBooking: true,
      });
      // 显示提示信息
      wx.showToast({
        title: '请完善手机号信息',
        icon: 'none',
        duration: 2000,
      });
    }
    
    // 加载用户信息
    this.loadUserInfo();
  },

  /**
   * 加载用户信息
   */
  async loadUserInfo() {
    try {
      const userInfo = await authApi.getUserInfo({
        showLoading: true,
        showError: true,
      });
      
      if (userInfo) {
        // 更新本地用户信息
        auth.setUserInfo(userInfo);
        const app = getApp();
        if (app && app.setUserInfo) {
          app.setUserInfo(userInfo);
        }
        
        this.setData({
          nickname: userInfo.nickname || '',
          phone: userInfo.phone || '',
        });
      }
    } catch (error) {
      console.error('加载用户信息失败:', error);
      // 如果加载失败，尝试从本地获取
      const localUserInfo = auth.getUserInfo();
      if (localUserInfo) {
        this.setData({
          nickname: localUserInfo.nickname || '',
          phone: localUserInfo.phone || '',
        });
      }
    }
  },

  /**
   * 输入昵称
   */
  onNicknameInput(e) {
    this.setData({
      nickname: e.detail.value,
    });
  },

  /**
   * 输入手机号
   */
  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value,
    });
  },

  /**
   * 保存用户信息
   */
  async onSave() {
    // 验证手机号
    if (!this.data.phone || !this.data.phone.trim()) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none',
      });
      return;
    }

    // 验证手机号格式
    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(this.data.phone.trim())) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none',
      });
      return;
    }

    try {
      wx.showLoading({
        title: '保存中...',
        mask: true,
      });

      // 构建用户信息
      const userInfo = {
        phone: this.data.phone.trim(),
      };
      
      // 如果昵称有变化，也更新昵称
      if (this.data.nickname && this.data.nickname.trim()) {
        userInfo.nickname = this.data.nickname.trim();
      }

      // 更新用户信息
      await userApi.updateUserInfo(userInfo);
      
      // 重新获取用户信息以更新本地缓存
      const updatedUserInfo = await authApi.getUserInfo({
        showLoading: false,
        showError: false,
      });
      
      if (updatedUserInfo) {
        auth.setUserInfo(updatedUserInfo);
        const app = getApp();
        if (app && app.setUserInfo) {
          app.setUserInfo(updatedUserInfo);
        }
      }

      wx.hideLoading();
      wx.showToast({
        title: '保存成功',
        icon: 'success',
      });

      setTimeout(() => {
        // 如果是从预订页面跳转过来的，返回上一页
        if (this.data.fromBooking) {
          wx.navigateBack();
        } else {
          // 否则返回上一页
          wx.navigateBack();
        }
      }, 1500);
    } catch (error) {
      wx.hideLoading();
      console.error('保存用户信息失败:', error);
      wx.showToast({
        title: error.message || '保存失败',
        icon: 'none',
      });
    }
  },
});
