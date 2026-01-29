/**
 * 景点详情页（独立页面）
 * 只负责景点相关逻辑，与酒店 / 商品详情完全分离
 */

const productApi = require('../../api/product');
const orderApi = require('../../api/order');
const auth = require('../../utils/auth');
const { normalizeUrl } = require('../../utils/url');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 景点ID
    id: null,

    // 详情数据
    detail: null,
    // 加载状态
    loading: false,

    // 图片/视频列表
    mediaList: [],
    // 当前轮播索引
    currentSwiperIndex: 0,

    // 详情标签页索引
    activeTab: 0,
    // 详情标签页列表
    tabs: ['详情', '评价', '须知'],
    // 主 Tab：booking-门票预订，intro-景区介绍
    bookingMainTab: 'booking',

    // 最小日期（今天）
    minDate: '',
    // 可选日期列表（最近四天）
    dateOptions: [],
    // 已选日期下标（最近四天卡片），-1 表示通过“更多日期”选择
    selectedDateIndex: 0,
    // 使用日期
    useDate: '',

    // 通用数量（暂用于兜底）
    quantity: 1,
    // 每个票种的数量映射：{ [ticketId]: number }
    ticketQuantities: {},
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('景点详情页加载', options);

    const id = options && options.id ? parseInt(options.id, 10) : null;
    if (!id) {
      wx.showToast({
        title: '景点ID不能为空',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack({
          fail() {
            wx.switchTab({
              url: '/pages/home/index',
            });
          },
        });
      }, 1500);
      return;
    }

    // 设置最小日期（今天）
    const today = new Date();
    const minDateStr = `${today.getFullYear()}-${String(
      today.getMonth() + 1
    ).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    const dateOptions = this.generateDateOptions(today);

    this.setData({
      id,
      minDate: minDateStr,
      bookingMainTab: 'booking',
      dateOptions,
      useDate: dateOptions[0]?.fullDate || '',
    });

    // 设置导航栏标题
    wx.setNavigationBarTitle({
      title: '景点详情',
    });

    // 加载详情数据
    this.loadDetail();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 设置分享按钮
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline'],
    });
  },

  /**
   * 加载景点详情
   */
  async loadDetail() {
    const { id } = this.data;

    this.setData({ loading: true });

    try {
      let detail = await productApi.getAttractionDetail(id);

      console.log('========== 景点详情数据 ==========');
      console.log('详情数据:', JSON.stringify(detail, null, 2));
      console.log('景点名称:', detail?.name);
      console.log('景点价格(原始):', detail?.price);
      console.log('景点票价(ticketPrice):', detail?.ticketPrice);
      console.log('景点图片:', detail?.images);
      console.log('景点描述:', detail?.description);
      console.log('================================');

      // 统一处理展示价格：优先 ticketPrice，其次 price / minPrice
      if (detail) {
        let displayPrice = detail.price;

        if (displayPrice === undefined || displayPrice === null || displayPrice === '') {
          if (detail.ticketPrice != null) {
            displayPrice = detail.ticketPrice;
          } else if (detail.minPrice != null) {
            displayPrice = detail.minPrice;
          } else {
            displayPrice = 0;
          }
        }

        detail.price = Number(displayPrice || 0);

        console.log('景点价格(用于展示):', detail.price, {
          originalPrice: detail.originalPrice,
          ticketPrice: detail.ticketPrice,
          minPrice: detail.minPrice,
        });
      }

      // 处理媒体列表
      const mediaList = this.processMediaList(detail);

      this.setData({
        detail,
        mediaList,
        loading: false,
      });
    } catch (error) {
      console.error('加载景点详情失败', error);
      this.setData({ loading: false });
      wx.showToast({
        title: error.message || '加载失败',
        icon: 'none',
      });
    }
  },

  /**
   * 生成最近四天的日期选项
   */
  generateDateOptions(baseDate = new Date()) {
    const weekMap = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
    const labelMap = ['今天', '明天', '后天'];

    const options = [];

    for (let i = 0; i < 4; i++) {
      const d = new Date(baseDate);
      d.setDate(d.getDate() + i);

      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');

      options.push({
        label: labelMap[i] || weekMap[d.getDay()],
        monthDay: `${d.getMonth() + 1}-${d.getDate()}`,
        fullDate: `${year}-${month}-${day}`,
        weekday: weekMap[d.getDay()],
      });
    }

    return options;
  },

  /**
   * 处理媒体列表（图片和视频）
   */
  processMediaList(detail) {
    const mediaList = [];

    if (!detail) return mediaList;

    // 图片
    if (detail.images && detail.images.length > 0) {
      detail.images.forEach((img) => {
        mediaList.push({
          type: 'image',
          url: normalizeUrl(img),
        });
      });
    } else if (detail.image) {
      mediaList.push({
        type: 'image',
        url: normalizeUrl(detail.image),
      });
    }

    // 视频
    if (detail.video) {
      mediaList.push({
        type: 'video',
        url: normalizeUrl(detail.video),
        poster: normalizeUrl(detail.videoPoster || detail.image || ''),
      });
    }

    return mediaList;
  },

  /**
   * 轮播切换事件
   */
  onSwiperChange(e) {
    this.setData({
      currentSwiperIndex: e.detail.current,
    });
  },

  /**
   * 图片点击预览
   */
  onImageTap(e) {
    const { index } = e.currentTarget.dataset;
    const { mediaList } = this.data;

    const urls = mediaList
      .filter((item) => item.type === 'image')
      .map((item) => item.url);

    if (urls.length > 0) {
      wx.previewImage({
        current: urls[index] || urls[0],
        urls,
      });
    }
  },

  onVideoPlay(e) {
    console.log('视频播放', e);
  },

  onVideoFullscreenChange(e) {
    console.log('视频全屏变化', e.detail);
  },

  /**
   * 顶部 Tab 切换（门票预订 / 景区介绍）
   */
  onBookingMainTabChange(e) {
    const { tab } = e.currentTarget.dataset;
    if (!tab) return;
    this.setData({
      bookingMainTab: tab,
    });
  },

  /**
   * 内部详情 Tab 切换（详情 / 评价 / 须知）
   */
  onTabChange(e) {
    const { index } = e.currentTarget.dataset;
    this.setData({
      activeTab: parseInt(index, 10),
    });
  },

  /**
   * 选择使用日期（picker）
   */
  onUseDateChange(e) {
    const useDate = e.detail.value;
    this.setData({
      useDate,
      selectedDateIndex: -1,
    });
  },

  /**
   * 点击最近四天日期卡片
   */
  onSelectUseDateTab(e) {
    const { index } = e.currentTarget.dataset;
    const { dateOptions } = this.data;
    const option = dateOptions[index];
    if (!option) return;

    this.setData({
      selectedDateIndex: index,
      useDate: option.fullDate,
    });
  },

  /**
   * 通用数量减少（兜底用）
   */
  onDecreaseQuantity() {
    const { quantity } = this.data;
    if (quantity <= 1) return;
    this.setData({
      quantity: quantity - 1,
    });
  },

  /**
   * 通用数量增加（兜底用）
   */
  onIncreaseQuantity() {
    const { quantity, detail } = this.data;
    if (detail && detail.stock !== undefined && quantity >= detail.stock) {
      wx.showToast({
        title: `库存不足，仅剩${detail.stock}件`,
        icon: 'none',
      });
      return;
    }
    this.setData({
      quantity: quantity + 1,
    });
  },

  /**
   * 获取指定票种数量，默认 1
   */
  getTicketQuantity(ticketId) {
    const { ticketQuantities } = this.data;
    if (!ticketId) return 1;
    const current = ticketQuantities[ticketId];
    if (typeof current !== 'number' || current <= 0) {
      return 1;
    }
    return current;
  },

  /**
   * 设置指定票种数量
   */
  setTicketQuantity(ticketId, quantity) {
    if (!ticketId) return;
    let finalQty = parseInt(quantity, 10);
    if (isNaN(finalQty) || finalQty <= 0) {
      finalQty = 1;
    }
    const { ticketQuantities } = this.data;
    this.setData({
      ticketQuantities: {
        ...ticketQuantities,
        [ticketId]: finalQty,
      },
    });
  },

  /**
   * 减少票种数量
   */
  onDecreaseTicketQuantity(e) {
    const { ticketId } = e.currentTarget.dataset;
    const { detail } = this.data;
    if (!ticketId) return;

    const current = this.getTicketQuantity(ticketId);
    if (current <= 1) {
      return;
    }

    // 校验库存（如果有的话）
    let maxStock = detail && detail.stock;
    if (detail && detail.ticketCategories) {
      detail.ticketCategories.forEach((cat) => {
        if (cat && cat.tickets) {
          cat.tickets.forEach((t) => {
            if (t && String(t.id) === String(ticketId) && t.stock !== undefined) {
              maxStock = t.stock;
            }
          });
        }
      });
    }

    if (maxStock !== undefined && current > maxStock) {
      wx.showToast({
        title: `库存不足，仅剩${maxStock}张`,
        icon: 'none',
      });
      this.setTicketQuantity(ticketId, maxStock);
      return;
    }

    this.setTicketQuantity(ticketId, current - 1);
  },

  /**
   * 增加票种数量
   */
  onIncreaseTicketQuantity(e) {
    const { ticketId } = e.currentTarget.dataset;
    const { detail } = this.data;
    if (!ticketId) return;

    const current = this.getTicketQuantity(ticketId);
    let next = current + 1;

    let maxStock = detail && detail.stock;
    if (detail && detail.ticketCategories) {
      detail.ticketCategories.forEach((cat) => {
        if (cat && cat.tickets) {
          cat.tickets.forEach((t) => {
            if (t && String(t.id) === String(ticketId) && t.stock !== undefined) {
              maxStock = t.stock;
            }
          });
        }
      });
    }

    if (maxStock !== undefined && next > maxStock) {
      wx.showToast({
        title: `库存不足，仅剩${maxStock}张`,
        icon: 'none',
      });
      next = maxStock;
    }

    this.setTicketQuantity(ticketId, next);
  },

  /**
   * 点击票种上的「预订」按钮
   */
  async onTicketBookTap(e) {
    const { ticket } = e.currentTarget.dataset;
    const { useDate, id } = this.data;

    if (!ticket) {
      wx.showToast({
        title: '票种信息异常',
        icon: 'none',
      });
      return;
    }

    if (!useDate) {
      wx.showToast({
        title: '请选择使用日期',
        icon: 'none',
      });
      return;
    }

    if (ticket.status !== undefined && ticket.status !== 1) {
      wx.showToast({
        title: '该票种暂不可售',
        icon: 'none',
      });
      return;
    }

    try {
      // 检查登录
      if (!auth.isLoggedIn()) {
        wx.showModal({
          title: '提示',
          content: '请先登录后再购买',
          confirmText: '去登录',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({
                url: '/pages/mine/login',
              });
            }
          },
        });
        return;
      }

      const userInfo = auth.getUserInfo();
      const contactName = userInfo?.nickname || userInfo?.name || '微信用户';
      const contactPhone = userInfo?.phone || '';

      if (!contactPhone) {
        wx.showModal({
          title: '提示',
          content: '请先完善手机号信息',
          confirmText: '去完善',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({
                url: '/pages/mine/profile-edit?fromBooking=true',
              });
            }
          },
        });
        return;
      }

      // 每个票种使用自己的数量，默认为 1
      const ticketQuantity = this.getTicketQuantity(ticket.id);

      const orderItem = {
        itemType: 'ATTRACTION_TICKET',
        itemId: ticket.id,
        quantity: ticketQuantity,
        useDate,
      };

      const orderData = {
        orderType: 'ATTRACTION',
        items: [orderItem],
        contactName,
        contactPhone,
        attractionId: id,
      };

      wx.showLoading({
        title: '创建订单中...',
        mask: true,
      });

      const order = await orderApi.createOrder(orderData);
      wx.hideLoading();

      wx.navigateTo({
        url: `/pages/order/detail?id=${order.id}`,
      });
    } catch (error) {
      wx.hideLoading();
      if (error.isAuthError) {
        return;
      }
      console.error('创建景点票种订单失败', error);
      wx.showToast({
        title: error.message || '创建订单失败',
        icon: 'none',
      });
    }
  },

  /**
   * 点击票种上的「立即支付」按钮
   */
  async onTicketPayTap(e) {
    const { ticket } = e.currentTarget.dataset;
    const { useDate, id } = this.data;

    if (!ticket) {
      wx.showToast({
        title: '票种信息异常',
        icon: 'none',
      });
      return;
    }

    if (!useDate) {
      wx.showToast({
        title: '请选择使用日期',
        icon: 'none',
      });
      return;
    }

    if (ticket.status !== undefined && ticket.status !== 1) {
      wx.showToast({
        title: '该票种暂不可售',
        icon: 'none',
      });
      return;
    }

    try {
      // 检查登录
      if (!auth.isLoggedIn()) {
        wx.showModal({
          title: '提示',
          content: '请先登录后再购买',
          confirmText: '去登录',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({
                url: '/pages/mine/login',
              });
            }
          },
        });
        return;
      }

      const userInfo = auth.getUserInfo();
      const contactName = userInfo?.nickname || userInfo?.name || '微信用户';
      const contactPhone = userInfo?.phone || '';

      if (!contactPhone) {
        wx.showModal({
          title: '提示',
          content: '请先完善手机号信息',
          confirmText: '去完善',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({
                url: '/pages/mine/profile-edit?fromBooking=true',
              });
            }
          },
        });
        return;
      }

      // 每个票种使用自己的数量，默认为 1
      const ticketQuantity = this.getTicketQuantity(ticket.id);

      const orderItem = {
        itemType: 'ATTRACTION_TICKET',
        itemId: ticket.id,
        quantity: ticketQuantity,
        useDate,
      };

      const orderData = {
        orderType: 'ATTRACTION',
        items: [orderItem],
        contactName,
        contactPhone,
        attractionId: id,
      };

      wx.showLoading({
        title: '创建订单中...',
        mask: true,
      });

      // 创建订单
      const order = await orderApi.createOrder(orderData);
      console.log('订单创建成功:', order);

      // 调用支付（支付失败会在 payOrder 方法内部处理并跳转）
      await this.payOrder(order.id);

    } catch (error) {
      wx.hideLoading();
      if (error.isAuthError) {
        return;
      }
      console.error('创建景点票种订单失败', error);
      wx.showToast({
        title: error.message || '创建订单失败',
        icon: 'none',
      });
    }
  },

  /**
   * 支付订单
   */
  async payOrder(orderId) {
    if (!orderId) {
      console.error('订单ID不能为空');
      return;
    }

    try {
      wx.showLoading({
        title: '支付中...',
        mask: true,
      });

      // 调用支付接口
      const paymentParams = await orderApi.payOrder(orderId, {
        payType: 'WECHAT',
      });

      wx.hideLoading();

      console.log('支付参数:', paymentParams);

      // 调用微信支付
      wx.requestPayment({
        timeStamp: paymentParams.timeStamp,
        nonceStr: paymentParams.nonceStr,
        package: paymentParams.package,
        signType: paymentParams.signType,
        paySign: paymentParams.paySign,
        success: (res) => {
          console.log('支付成功:', res);
          wx.showToast({
            title: '支付成功',
            icon: 'success',
          });
          
          // 跳转到订单详情页
          setTimeout(() => {
            wx.redirectTo({
              url: `/pages/order/detail?id=${orderId}`,
            });
          }, 1500);
        },
        fail: (err) => {
          console.error('支付失败:', err);
          wx.hideLoading();
          
          const isCancel = err.errMsg && err.errMsg.includes('cancel');
          
          // 支付失败跳转到我的订单的待支付列表页
          console.log('支付失败，准备跳转到订单列表页...');
          wx.navigateTo({
            url: '/pages/order/index?status=PENDING_PAY',
            success: () => {
              console.log('跳转订单列表页成功');
              if (isCancel) {
                wx.showToast({
                  title: '支付已取消',
                  icon: 'none',
                });
              } else {
                wx.showToast({
                  title: '支付失败，请重试',
                  icon: 'none',
                });
              }
            },
            fail: (navErr) => {
              console.error('跳转订单列表页失败:', navErr);
              // 如果 navigateTo 失败，尝试使用 reLaunch
              wx.reLaunch({
                url: '/pages/mine/index',
                success: () => {
                  setTimeout(() => {
                    wx.navigateTo({
                      url: '/pages/order/index?status=PENDING_PAY',
                    });
                  }, 300);
                },
                fail: () => {
                  wx.showToast({
                    title: isCancel ? '支付已取消' : '支付失败，请重试',
                    icon: 'none',
                  });
                },
              });
            },
          });
        },
      });
    } catch (error) {
      wx.hideLoading();
      console.error('支付失败:', error);
      
      // 支付失败跳转到我的订单的待支付列表页
      console.log('支付接口调用失败，准备跳转到订单列表页...');
      wx.navigateTo({
        url: '/pages/order/index?status=PENDING_PAY',
        success: () => {
          console.log('跳转订单列表页成功（catch）');
          wx.showToast({
            title: error.message || '支付失败',
            icon: 'none',
            duration: 2000,
          });
        },
        fail: (navErr) => {
          console.error('跳转订单列表页失败（catch）:', navErr);
          // 如果 navigateTo 失败，尝试使用 reLaunch
          wx.reLaunch({
            url: '/pages/mine/index',
            success: () => {
              setTimeout(() => {
                wx.navigateTo({
                  url: '/pages/order/index?status=PENDING_PAY',
                });
              }, 300);
            },
            fail: () => {
              wx.showToast({
                title: error.message || '支付失败',
                icon: 'none',
                duration: 2000,
              });
            },
          });
        },
      });
    }
  },

  /**
   * 联系客服（预留）
   */
  onContactService() {
    wx.showToast({
      title: '客服功能开发中',
      icon: 'none',
    });
  },

  onGoHome() {
    wx.switchTab({
      url: '/pages/home/index',
    });
  },

  onGoCart() {
    wx.switchTab({
      url: '/pages/cart/index',
    });
  },

  /**
   * 分享给好友
   */
  onShareAppMessage() {
    const { id, detail } = this.data;
    return {
      title: detail?.name || '景点详情',
      path: `/pages/attraction/detail?id=${id}`,
      imageUrl: detail?.image || '',
    };
  },

  /**
   * 分享到朋友圈
   */
  onShareTimeline() {
    const { id, detail } = this.data;
    return {
      title: detail?.name || '景点详情',
      query: `id=${id}`,
      imageUrl: detail?.image || '',
    };
  },
});
