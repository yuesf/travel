/**
 * 订单列表页
 */

const orderApi = require('../../api/order');
const auth = require('../../utils/auth');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 订单状态标签
    statusTabs: [
      { key: '', label: '全部' },
      { key: 'PENDING_PAY', label: '待支付' },
      { key: 'PAID', label: '待使用' },
      { key: 'COMPLETED', label: '已完成' },
      { key: 'CANCELLED', label: '已取消' },
    ],
    currentStatus: '', // 当前选中的状态
    orderList: [], // 订单列表
    loading: false, // 加载状态
    hasMore: true, // 是否还有更多数据
    page: 1, // 当前页码
    pageSize: 10, // 每页数量
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('订单列表页加载');
    
    // 从页面参数获取状态
    // 验证 status 是否为有效的字符串，排除 "[object Object]" 等无效值
    let status = options.status || '';
    if (typeof status !== 'string' || status === '[object Object]' || status.trim() === '') {
      status = '';
    }
    
    // 验证 status 是否为有效的订单状态值
    const validStatuses = ['', 'PENDING_PAY', 'PAID', 'COMPLETED', 'CANCELLED'];
    if (!validStatuses.includes(status)) {
      status = '';
    }
    
    this.setData({
      currentStatus: status,
    });
    
    // 加载订单列表
    this.loadOrderList(true);
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 刷新订单列表
    this.loadOrderList(true);
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadOrderList(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadOrderList(false);
    }
  },

  /**
   * 加载订单列表
   * @param {boolean} refresh 是否刷新（重置页码）
   */
  async loadOrderList(refresh = false) {
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

    const page = refresh ? 1 : this.data.page;
    
    this.setData({
      loading: true,
    });

    try {
      const params = {
        page,
        pageSize: this.data.pageSize,
      };
      
      if (this.data.currentStatus) {
        params.status = this.data.currentStatus;
      }

      const result = await orderApi.getOrderList(params);
      console.log('订单列表:', result);

      // 处理返回的数据格式
      // 后端返回的是 PageResult 对象，包含 list、total、page、pageSize 字段
      const list = result?.list || result?.data || (Array.isArray(result) ? result : []);
      const total = result?.total || 0;
      
      // 格式化订单数据
      const formattedList = list.map(order => this.formatOrderData(order));
      const orderList = refresh ? formattedList : [...this.data.orderList, ...formattedList];
      const hasMore = orderList.length < total;

      this.setData({
        orderList,
        page: page + 1,
        hasMore,
        loading: false,
      });
    } catch (error) {
      console.error('加载订单列表失败:', error);
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 切换订单状态标签
   */
  onStatusTabChange(e) {
    const index = e.currentTarget.dataset.index;
    const status = this.data.statusTabs[index].key;
    
    if (status === this.data.currentStatus) {
      return;
    }

    this.setData({
      currentStatus: status,
      orderList: [],
      page: 1,
      hasMore: true,
    });

    // 重新加载订单列表
    this.loadOrderList(true);
  },

  /**
   * 去逛逛
   */
  onGoShopping() {
    wx.switchTab({
      url: '/pages/home/index',
    });
  },

  /**
   * 点击订单卡片
   */
  onOrderTap(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/detail?id=${orderId}`,
    });
  },

  /**
   * 取消订单
   */
  async onCancelOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await orderApi.cancelOrder(orderId);
            wx.showToast({
              title: '订单已取消',
              icon: 'success',
            });
            
            // 刷新订单列表
            this.loadOrderList(true);
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
  onPayOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/detail?id=${orderId}&action=pay`,
    });
  },

  /**
   * 申请退款
   */
  async onApplyRefund(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '申请退款',
      content: '确定要申请退款吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await orderApi.applyRefund(orderId);
            wx.showToast({
              title: '退款申请已提交',
              icon: 'success',
            });
            
            // 刷新订单列表
            this.loadOrderList(true);
          } catch (error) {
            console.error('申请退款失败:', error);
          }
        }
      },
    });
  },

  /**
   * 格式化订单数据
   */
  formatOrderData(order) {
    const statusMap = {
      PENDING_PAY: '待支付',
      PAID: '已支付',
      USED: '已使用',
      COMPLETED: '已完成',
      CANCELLED: '已取消',
      REFUNDED: '已退款',
    };

    const typeMap = {
      ATTRACTION: '景点',
      HOTEL: '酒店',
      PRODUCT: '商品',
    };

    // 格式化订单项数据
    if (order.items && Array.isArray(order.items)) {
      order.items = order.items.map(item => {
        return {
          ...item,
          quantity: item.quantity || 1,
          price: this.formatPrice(item.price),
          totalPrice: this.formatPrice(item.totalPrice),
          itemImage: item.itemImage || item.image || '',
        };
      });
    }

    return {
      ...order,
      statusText: statusMap[order.status] || order.status,
      orderTypeText: typeMap[order.orderType] || order.orderType,
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
    const orderIndex = e.currentTarget.dataset.orderIndex;
    const itemIndex = e.currentTarget.dataset.itemIndex;
    const orderList = this.data.orderList || [];
    
    if (orderList[orderIndex] && orderList[orderIndex].items && orderList[orderIndex].items[itemIndex]) {
      orderList[orderIndex].items[itemIndex].itemImage = '/assets/icons/default-product.png';
      this.setData({
        orderList: orderList,
      });
    }
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
