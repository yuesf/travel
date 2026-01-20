/**
 * 收货地址列表页
 */

const addressApi = require('../../api/address');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    addressList: [], // 收货地址列表
    selectMode: false, // 是否为选择模式（从订单确认页跳转）
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('收货地址列表页加载');
    
    // 检查是否为选择模式
    if (options.select === 'true') {
      this.setData({
        selectMode: true,
      });
    }
    
    this.loadAddressList();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.loadAddressList();
  },

  /**
   * 加载收货地址列表
   */
  async loadAddressList() {
    try {
      const addressList = await addressApi.getAddressList();
      this.setData({
        addressList,
      });
    } catch (error) {
      console.error('加载收货地址列表失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none',
      });
    }
  },

  /**
   * 添加收货地址
   */
  onAddAddress() {
    wx.navigateTo({
      url: '/pages/mine/address-edit',
    });
  },

  /**
   * 编辑收货地址
   */
  onEditAddress(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/mine/address-edit?id=${id}`,
    });
  },

  /**
   * 选择收货地址（选择模式）
   */
  onSelectAddress(e) {
    if (!this.data.selectMode) {
      return;
    }
    
    const id = e.currentTarget.dataset.id;
    const address = this.data.addressList.find(addr => addr.id === id);
    
    if (address) {
      // 返回上一页并传递选中的地址
      const pages = getCurrentPages();
      if (pages.length >= 2) {
        const prevPage = pages[pages.length - 2];
        // 直接设置上一页的数据
        if (prevPage && prevPage.setData) {
          prevPage.setData({
            selectedAddress: address,
            contactName: address.receiverName,
            contactPhone: address.receiverPhone,
          });
        }
        wx.navigateBack({
          delta: 1,
        });
      }
    }
  },

  /**
   * 设置默认地址
   */
  async onSetDefault(e) {
    const id = e.currentTarget.dataset.id;
    try {
      await addressApi.setDefaultAddress(id);
      wx.showToast({
        title: '设置成功',
        icon: 'success',
      });
      this.loadAddressList();
    } catch (error) {
      console.error('设置默认地址失败:', error);
      wx.showToast({
        title: '设置失败',
        icon: 'none',
      });
    }
  },

  /**
   * 删除收货地址
   */
  onDeleteAddress(e) {
    const id = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '提示',
      content: '确定要删除该收货地址吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await addressApi.deleteAddress(id);
            wx.showToast({
              title: '删除成功',
              icon: 'success',
            });
            this.loadAddressList();
          } catch (error) {
            console.error('删除收货地址失败:', error);
            wx.showToast({
              title: '删除失败',
              icon: 'none',
            });
          }
        }
      },
    });
  },
});
