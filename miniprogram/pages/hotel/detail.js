/**
 * 酒店详情页（独立）
 * 只负责酒店相关展示与预订逻辑
 */

const productApi = require('../../api/product');
const orderApi = require('../../api/order');
const auth = require('../../utils/auth');
const { normalizeUrl } = require('../../utils/url');

Page({
  data: {
    id: null,
    detail: null,
    loading: false,

    mediaList: [],
    currentSwiperIndex: 0,

    // 入住/退房日期
    minDate: '',
    checkInDate: '',
    checkOutDate: '',

    // 房型选择
    selectedRoom: null,
    quantity: 1,

    // 房型弹窗
    showRoomModal: false,
  },

  onLoad(options) {
    console.log('酒店详情页加载', options);
    const id = options && options.id ? parseInt(options.id, 10) : null;
    if (!id) {
      wx.showToast({
        title: '酒店ID不能为空',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack({
          fail() {
            wx.switchTab({ url: '/pages/home/index' });
          },
        });
      }, 1500);
      return;
    }

    // 最小日期（今天）
    const today = new Date();
    const minDateStr = `${today.getFullYear()}-${String(
      today.getMonth() + 1
    ).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

    this.setData({
      id,
      minDate: minDateStr,
    });

    wx.setNavigationBarTitle({
      title: '酒店详情',
    });

    this.loadDetail();
  },

  onShow() {
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline'],
    });
  },

  async loadDetail() {
    const { id } = this.data;
    this.setData({ loading: true });

    try {
      const detail = await productApi.getHotelDetail(id);

      console.log('========== 酒店详情数据 ==========');
      console.log('详情数据:', JSON.stringify(detail, null, 2));
      console.log('酒店名称:', detail?.name);
      console.log('酒店价格(原始):', detail?.price);
      console.log('酒店最小价(minPrice):', detail?.minPrice);
      console.log('酒店图片:', detail?.images);
      console.log('================================');

      // 统一展示价：优先 minPrice，其次 price
      if (detail) {
        let displayPrice = detail.price;
        if (displayPrice === undefined || displayPrice === null || displayPrice === '') {
          if (detail.minPrice != null) {
            displayPrice = detail.minPrice;
          } else {
            displayPrice = 0;
          }
        }
        detail.price = Number(displayPrice || 0);
      }

      const mediaList = this.processMediaList(detail);

      this.setData({
        detail,
        mediaList,
        loading: false,
      });
    } catch (error) {
      console.error('加载酒店详情失败', error);
      this.setData({ loading: false });
      wx.showToast({
        title: error.message || '加载失败',
        icon: 'none',
      });
    }
  },

  processMediaList(detail) {
    const mediaList = [];
    if (!detail) return mediaList;

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

    if (detail.video) {
      mediaList.push({
        type: 'video',
        url: normalizeUrl(detail.video),
        poster: normalizeUrl(detail.videoPoster || detail.image || ''),
      });
    }

    return mediaList;
  },

  onSwiperChange(e) {
    this.setData({
      currentSwiperIndex: e.detail.current,
    });
  },

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

  // 选择房型（直接点击列表）
  onSelectRoom(e) {
    const { room } = e.currentTarget.dataset;
    this.setData({
      selectedRoom: room,
      showRoomModal: true,
    });
  },

  onCloseRoomModal() {
    this.setData({
      showRoomModal: false,
    });
  },

  onPreventMove() {
    return false;
  },

  onCheckInDateChange(e) {
    const checkInDate = e.detail.value;
    const { checkOutDate } = this.data;

    wx.nextTick(() => {
      if (!checkOutDate || checkOutDate <= checkInDate) {
        const checkInDateObj = new Date(checkInDate);
        checkInDateObj.setDate(checkInDateObj.getDate() + 1);
        const nextDay = checkInDateObj.toISOString().split('T')[0];

        this.setData({
          checkInDate,
          checkOutDate: nextDay,
        });
      } else {
        this.setData({
          checkInDate,
        });
      }
    });
  },

  onCheckOutDateChange(e) {
    const checkOutDate = e.detail.value;
    const { checkInDate } = this.data;

    if (checkInDate && checkOutDate <= checkInDate) {
      wx.showToast({
        title: '退房日期必须晚于入住日期',
        icon: 'none',
      });
      return;
    }

    this.setData({
      checkOutDate,
    });
  },

  onDecreaseQuantity() {
    const { quantity } = this.data;
    if (quantity <= 1) return;
    this.setData({
      quantity: quantity - 1,
    });
  },

  onIncreaseQuantity() {
    const { quantity, selectedRoom, detail } = this.data;

    let stock = detail && detail.stock;
    if (selectedRoom && selectedRoom.stock != null) {
      stock = selectedRoom.stock;
    }

    if (stock != null && quantity >= stock) {
      wx.showToast({
        title: `库存不足，仅剩${stock}间`,
        icon: 'none',
      });
      return;
    }

    this.setData({
      quantity: quantity + 1,
    });
  },

  async onBookNow() {
    if (!auth.isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再预订',
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

    const { id, selectedRoom, checkInDate, checkOutDate, quantity } = this.data;

    if (!selectedRoom) {
      wx.showToast({
        title: '请选择房型',
        icon: 'none',
      });
      this.setData({ showRoomModal: true });
      return;
    }
    if (!checkInDate || !checkOutDate) {
      wx.showToast({
        title: '请选择入住和退房日期',
        icon: 'none',
      });
      this.setData({ showRoomModal: true });
      return;
    }

    try {
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

      const orderItem = {
        itemType: 'HOTEL_ROOM',
        itemId: selectedRoom.id,
        quantity,
        checkInDate,
        checkOutDate,
      };

      const orderData = {
        orderType: 'HOTEL',
        items: [orderItem],
        contactName,
        contactPhone,
        hotelId: id,
      };

      wx.showLoading({
        title: '创建订单中...',
        mask: true,
      });

      const order = await orderApi.createOrder(orderData);
      console.log('酒店订单创建成功:', order);

      wx.hideLoading();

      this.setData({
        showRoomModal: false,
      });

      wx.navigateTo({
        url: `/pages/order/detail?id=${order.id}`,
      });
    } catch (error) {
      wx.hideLoading();
      if (error.isAuthError) return;
      console.error('创建酒店订单失败', error);
      wx.showToast({
        title: error.message || '创建订单失败',
        icon: 'none',
      });
    }
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

  onShareAppMessage() {
    const { id, detail } = this.data;
    return {
      title: detail?.name || '酒店详情',
      path: `/pages/hotel/detail?id=${id}`,
      imageUrl: detail?.image || '',
    };
  },

  onShareTimeline() {
    const { id, detail } = this.data;
    return {
      title: detail?.name || '酒店详情',
      query: `id=${id}`,
      imageUrl: detail?.image || '',
    };
  },
});

