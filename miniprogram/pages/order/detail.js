/**
 * 订单详情页
 */

const orderApi = require('../../api/order');
const auth = require('../../utils/auth');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    orderId: null, // 订单ID
    order: null, // 订单详情
    loading: false, // 加载状态
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('订单详情页加载');
    
    const orderId = options.id;
    if (!orderId) {
      wx.showToast({
        title: '订单ID不能为空',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }

    this.setData({
      orderId,
    });

    // 加载订单详情
    this.loadOrderDetail();

    // 如果需要支付，显示支付提示
    if (options.action === 'pay') {
      setTimeout(() => {
        wx.showToast({
          title: '请完成支付',
          icon: 'none',
        });
      }, 500);
    }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 刷新订单详情
    if (this.data.orderId) {
      this.loadOrderDetail();
    }
  },

  /**
   * 加载订单详情
   */
  async loadOrderDetail() {
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
      const order = await orderApi.getOrderDetail(this.data.orderId);
      console.log('订单详情:', order);

      // 格式化订单数据
      const formattedOrder = this.formatOrderData(order);

      this.setData({
        order: formattedOrder,
        loading: false,
      });
    } catch (error) {
      console.error('加载订单详情失败:', error);
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 格式化订单数据
   */
  formatOrderData(order) {
    const statusMap = {
      PENDING_PAY: { text: '待支付', desc: '请尽快完成支付', color: '#E6A23C' },
      PAID: { text: '已支付', desc: '订单已支付，等待使用', color: '#409EFF' },
      USED: { text: '已使用', desc: '订单已使用', color: '#67C23A' },
      COMPLETED: { text: '已完成', desc: '订单已完成', color: '#909399' },
      CANCELLED: { text: '已取消', desc: '订单已取消', color: '#F56C6C' },
      REFUNDED: { text: '已退款', desc: '订单已退款', color: '#F56C6C' },
    };

    const typeMap = {
      ATTRACTION: '景点',
      HOTEL: '酒店',
      PRODUCT: '商品',
    };

    const statusInfo = statusMap[order.status] || { text: order.status, desc: '', color: '#909399' };

    // 格式化订单项数据
    if (order.items && Array.isArray(order.items)) {
      order.items = order.items.map(item => {
        return {
          ...item,
          // 确保数量有默认值
          quantity: item.quantity || 1,
          // 格式化价格
          price: this.formatPrice(item.price),
          totalPrice: this.formatPrice(item.totalPrice),
          // 确保图片有默认值
          itemImage: item.itemImage || item.image || '',
        };
      });
    }

    return {
      ...order,
      statusText: statusInfo.text,
      statusDesc: statusInfo.desc,
      statusColor: statusInfo.color,
      orderTypeText: typeMap[order.orderType] || order.orderType,
      createTimeText: this.formatDateTime(order.createTime),
      payTimeText: order.payTime ? this.formatDateTime(order.payTime) : '',
      // 格式化订单价格
      totalAmount: this.formatPrice(order.totalAmount),
      discountAmount: this.formatPrice(order.discountAmount),
      payAmount: this.formatPrice(order.payAmount),
    };
  },

  /**
   * 格式化价格
   */
  formatPrice(price) {
    if (price == null || price === '') {
      return '0.00';
    }
    const num = parseFloat(price);
    if (isNaN(num)) {
      return '0.00';
    }
    return num.toFixed(2);
  },

  /**
   * 图片加载失败处理
   */
  onImageError(e) {
    const index = e.currentTarget.dataset.index;
    const items = this.data.order.items || [];
    if (items[index]) {
      items[index].itemImage = '/assets/icons/default-product.png';
      this.setData({
        'order.items': items,
      });
    }
  },

  /**
   * 取消订单
   */
  async onCancelOrder() {
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await orderApi.cancelOrder(this.data.orderId);
            wx.showToast({
              title: '订单已取消',
              icon: 'success',
            });
            
            // 刷新订单详情
            this.loadOrderDetail();
          } catch (error) {
            console.error('取消订单失败:', error);
          }
        }
      },
    });
  },

  /**
   * 支付订单
   */
  async onPayOrder() {
    // 跳转到支付页面或调用支付接口
    wx.showToast({
      title: '支付功能开发中',
      icon: 'none',
    });
    // TODO: 实现支付功能
  },

  /**
   * 申请退款
   */
  async onApplyRefund() {
    wx.showModal({
      title: '申请退款',
      content: '确定要申请退款吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await orderApi.applyRefund(this.data.orderId);
            wx.showToast({
              title: '退款申请已提交',
              icon: 'success',
            });
            
            // 刷新订单详情
            this.loadOrderDetail();
          } catch (error) {
            console.error('申请退款失败:', error);
          }
        }
      },
    });
  },

  /**
   * 格式化日期时间
   */
  formatDateTime(dateTime) {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  },
});
