/**
 * 购物车页
 */

const cartApi = require('../../api/cart');
const auth = require('../../utils/auth');
const constants = require('../../utils/constants');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    navBarTotalHeight: 0, // 导航栏总高度
    defaultProductImage: constants.DEFAULT_IMAGES.PRODUCT,
    cartList: [], // 购物车列表（包含商品详情）
    selectedAll: false, // 是否全选
    totalPrice: 0, // 总价
    selectedItemsCount: 0, // 选中商品数量
    editMode: false, // 是否编辑模式
    loading: false, // 加载状态
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('购物车页加载');
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    const navBarHelper = require('../../utils/nav-bar-helper');
    navBarHelper.initNavBar(this);
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 刷新购物车数据
    this.loadCartData();
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadCartData(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 加载购物车数据
   * @param {boolean} forceRefresh 是否强制刷新
   */
  async loadCartData(forceRefresh = false) {
    // 检查登录状态
    if (!auth.isLoggedIn()) {
      wx.showToast({
        title: '请先登录',
        icon: 'none',
      });
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/mine/index',
        });
      }, 1500);
      return;
    }

    if (this.data.loading) {
      return;
    }

    this.setData({
      loading: true,
    });

    try {
      // 从后端实时查询购物车列表（包含商品详情）
      const cartList = await cartApi.getCartList();
      console.log('购物车列表（包含商品详情）:', cartList);

      // 后端已返回包含商品详情的购物车数据，直接使用
      const cartListWithDetails = cartList.map((item) => ({
        ...item,
        selected: false, // 默认未选中
        productDetail: item.itemDetail, // 使用后端返回的商品详情
      }));

      this.setData({
        cartList: cartListWithDetails,
        loading: false,
      });

      // 更新选中状态和总价
      this.updateSelectedState();
      this.updateTotalPrice();

      // 更新全局购物车数量
      const app = getApp();
      if (app && app.setCartCount) {
        app.setCartCount(cartList.length);
      }
    } catch (error) {
      console.error('加载购物车数据失败:', error);
      this.setData({
        loading: false,
      });
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
      });
    }
  },


  /**
   * 切换编辑模式
   */
  onToggleEditMode() {
    this.setData({
      editMode: !this.data.editMode,
    });
  },

  /**
   * 去逛逛（空状态操作）
   */
  onGoShopping() {
    wx.switchTab({
      url: '/pages/home/index',
    });
  },

  /**
   * 全选/取消全选
   */
  onToggleSelectAll() {
    const selectedAll = !this.data.selectedAll;
    const cartList = this.data.cartList.map((item) => ({
      ...item,
      selected: selectedAll,
    }));

    this.setData({
      cartList,
      selectedAll,
    });

    this.updateTotalPrice();
  },

  /**
   * 切换单个商品选择状态
   * @param {Object} e 事件对象
   */
  onToggleItemSelect(e) {
    const { index } = e.currentTarget.dataset;
    const cartList = [...this.data.cartList];
    cartList[index].selected = !cartList[index].selected;

    this.setData({
      cartList,
    });

    this.updateSelectedState();
    this.updateTotalPrice();
  },

  /**
   * 更新选中状态（检查是否全选）
   */
  updateSelectedState() {
    const cartList = this.data.cartList;
    if (cartList.length === 0) {
      this.setData({
        selectedAll: false,
        selectedItemsCount: 0,
      });
      return;
    }

    const selectedAll = cartList.every((item) => item.selected);
    const selectedItemsCount = cartList.filter((item) => item.selected).length;
    this.setData({
      selectedAll,
      selectedItemsCount,
    });
  },

  /**
   * 更新总价（使用后端返回的商品价格）
   */
  updateTotalPrice() {
    const cartList = this.data.cartList;
    let totalPrice = 0;
    let selectedItemsCount = 0;

    cartList.forEach((item) => {
      if (item.selected) {
        // 优先使用后端返回的itemPrice，如果没有则使用productDetail中的价格
        const price = item.itemPrice || (item.productDetail && (item.productDetail.price || item.productDetail.minPrice)) || 0;
        totalPrice += price * item.quantity;
        selectedItemsCount += 1;
      }
    });

    this.setData({
      totalPrice: totalPrice.toFixed(2),
      selectedItemsCount,
    });
  },

  /**
   * 减少商品数量
   * @param {Object} e 事件对象
   */
  async onDecreaseQuantity(e) {
    const { index } = e.currentTarget.dataset;
    const cartItem = this.data.cartList[index];
    
    if (cartItem.quantity <= 1) {
      wx.showToast({
        title: '数量不能小于1',
        icon: 'none',
      });
      return;
    }

    await this.updateCartQuantity(cartItem.id, cartItem.quantity - 1, index);
  },

  /**
   * 增加商品数量
   * @param {Object} e 事件对象
   */
  async onIncreaseQuantity(e) {
    const { index } = e.currentTarget.dataset;
    const cartItem = this.data.cartList[index];
    
    // 验证库存（使用后端返回的itemStock）
    const stock = cartItem.itemStock || (cartItem.productDetail && (cartItem.productDetail.stock || cartItem.productDetail.availableStock)) || 999999;
      if (cartItem.quantity >= stock) {
        wx.showToast({
          title: '库存不足',
          icon: 'none',
        });
        return;
    }

    await this.updateCartQuantity(cartItem.id, cartItem.quantity + 1, index);
  },

  /**
   * 更新购物车商品数量（云购物车方案：立即保存到后端，保存成功后重新查询列表）
   * @param {number} cartId 购物车ID
   * @param {number} quantity 新数量
   * @param {number} index 商品索引
   */
  async updateCartQuantity(cartId, quantity, index) {
    try {
      wx.showLoading({
        title: '更新中...',
        mask: true,
      });

      // 立即保存到后端数据库
      await cartApi.updateCart(cartId, { quantity });

      // 保存成功后，重新查询购物车列表获取最新数据（云购物车方案）
      await this.loadCartData();

      wx.hideLoading();
      wx.showToast({
        title: '更新成功',
        icon: 'success',
        duration: 1500,
      });
    } catch (error) {
      console.error('更新购物车数量失败:', error);
      wx.hideLoading();
      wx.showToast({
        title: error.message || '更新失败',
        icon: 'none',
      });
    }
  },

  /**
   * 删除购物车商品
   * @param {Object} e 事件对象
   */
  async onDeleteItem(e) {
    const { index } = e.currentTarget.dataset;
    const cartItem = this.data.cartList[index];

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个商品吗？',
      success: async (res) => {
        if (res.confirm) {
          await this.deleteCartItem(cartItem.id, index);
        }
      },
    });
  },

  /**
   * 删除购物车商品（云购物车方案：删除后重新查询列表）
   * @param {number} cartId 购物车ID
   * @param {number} index 商品索引
   */
  async deleteCartItem(cartId, index) {
    try {
      wx.showLoading({
        title: '删除中...',
        mask: true,
      });

      // 从后端数据库删除
      await cartApi.deleteCart(cartId);

      // 删除成功后，重新查询购物车列表获取最新数据（云购物车方案）
      await this.loadCartData();

      wx.hideLoading();
      wx.showToast({
        title: '删除成功',
        icon: 'success',
        duration: 1500,
      });
    } catch (error) {
      console.error('删除购物车商品失败:', error);
      wx.hideLoading();
      wx.showToast({
        title: error.message || '删除失败',
        icon: 'none',
      });
    }
  },

  /**
   * 点击商品卡片（跳转到详情页）
   * @param {Object} e 事件对象
   */
  onItemTap(e) {
    const { index } = e.currentTarget.dataset;
    const cartItem = this.data.cartList[index];
    
    if (!cartItem || !cartItem.productDetail) {
      return;
    }

    let url = '';
    switch (cartItem.itemType) {
      case 'ATTRACTION':
        url = `/pages/detail/index?type=attraction&id=${cartItem.itemId}`;
        break;
      case 'HOTEL_ROOM':
        url = `/pages/detail/index?type=hotel&id=${cartItem.itemId}`;
        break;
      case 'PRODUCT':
        url = `/pages/detail/index?type=product&id=${cartItem.itemId}`;
        break;
      default:
        return;
    }

    if (url) {
      wx.navigateTo({
        url,
      });
    }
  },

  /**
   * 结算
   */
  onCheckout() {
    const selectedItems = this.data.cartList.filter((item) => item.selected);
    
    if (selectedItems.length === 0) {
      wx.showToast({
        title: '请选择要结算的商品',
        icon: 'none',
      });
      return;
    }

    // 构建订单确认页参数
    const cartIds = selectedItems.map((item) => item.id).join(',');
    
    wx.navigateTo({
      url: `/pages/order/confirm?cartIds=${cartIds}`,
    });
  },
});
