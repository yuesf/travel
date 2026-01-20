/**
 * 收货地址编辑页
 */

const addressApi = require('../../api/address');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    id: null, // 地址ID（编辑时）
    receiverName: '', // 收货人姓名
    receiverPhone: '', // 收货人手机号
    province: '', // 省份
    city: '', // 城市
    district: '', // 区县
    detailAddress: '', // 详细地址
    isDefault: 0, // 是否默认地址
    region: [], // 省市区数组
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.id) {
      // 编辑模式
      this.setData({
        id: options.id,
      });
      this.loadAddress();
    }
  },

  /**
   * 加载收货地址详情
   */
  async loadAddress() {
    try {
      const address = await addressApi.getAddressById(this.data.id);
      this.setData({
        receiverName: address.receiverName || '',
        receiverPhone: address.receiverPhone || '',
        province: address.province || '',
        city: address.city || '',
        district: address.district || '',
        detailAddress: address.detailAddress || '',
        isDefault: address.isDefault || 0,
        region: [address.province, address.city, address.district],
      });
    } catch (error) {
      console.error('加载收货地址失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none',
      });
    }
  },

  /**
   * 输入收货人姓名
   */
  onReceiverNameInput(e) {
    this.setData({
      receiverName: e.detail.value,
    });
  },

  /**
   * 输入收货人手机号
   */
  onReceiverPhoneInput(e) {
    this.setData({
      receiverPhone: e.detail.value,
    });
  },

  /**
   * 选择省市区
   */
  onRegionChange(e) {
    const region = e.detail.value;
    this.setData({
      region,
      province: region[0] || '',
      city: region[1] || '',
      district: region[2] || '',
    });
  },

  /**
   * 输入详细地址
   */
  onDetailAddressInput(e) {
    this.setData({
      detailAddress: e.detail.value,
    });
  },

  /**
   * 切换默认地址
   */
  onDefaultChange(e) {
    this.setData({
      isDefault: e.detail.value ? 1 : 0,
    });
  },

  /**
   * 保存地址
   */
  async onSave() {
    // 验证必填项
    if (!this.data.receiverName || !this.data.receiverName.trim()) {
      wx.showToast({
        title: '请输入收货人姓名',
        icon: 'none',
      });
      return;
    }

    if (!this.data.receiverPhone || !this.data.receiverPhone.trim()) {
      wx.showToast({
        title: '请输入收货人手机号',
        icon: 'none',
      });
      return;
    }

    // 验证手机号格式
    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(this.data.receiverPhone)) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none',
      });
      return;
    }

    if (!this.data.province || !this.data.city || !this.data.district) {
      wx.showToast({
        title: '请选择省市区',
        icon: 'none',
      });
      return;
    }

    if (!this.data.detailAddress || !this.data.detailAddress.trim()) {
      wx.showToast({
        title: '请输入详细地址',
        icon: 'none',
      });
      return;
    }

    try {
      const addressData = {
        receiverName: this.data.receiverName.trim(),
        receiverPhone: this.data.receiverPhone.trim(),
        province: this.data.province,
        city: this.data.city,
        district: this.data.district,
        detailAddress: this.data.detailAddress.trim(),
        isDefault: this.data.isDefault,
      };

      if (this.data.id) {
        // 更新地址
        await addressApi.updateAddress(this.data.id, addressData);
        wx.showToast({
          title: '更新成功',
          icon: 'success',
        });
      } else {
        // 创建地址
        await addressApi.createAddress(addressData);
        wx.showToast({
          title: '添加成功',
          icon: 'success',
        });
      }

      setTimeout(() => {
        // 检查是否从订单确认页跳转过来的
        const pages = getCurrentPages();
        if (pages.length >= 2) {
          const prevPage = pages[pages.length - 2];
          // 如果上一页是订单确认页，需要刷新地址列表
          if (prevPage.route === 'pages/order/confirm' && prevPage.loadAddress) {
            prevPage.loadAddress();
          }
        }
        wx.navigateBack();
      }, 1500);
    } catch (error) {
      console.error('保存收货地址失败:', error);
      wx.showToast({
        title: '保存失败',
        icon: 'none',
      });
    }
  },
});
