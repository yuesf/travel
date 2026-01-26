/**
 * 详情页
 * 支持景点、酒店、商品详情展示
 */

const productApi = require('../../api/product');
const cartApi = require('../../api/cart');
const auth = require('../../utils/auth');
const { normalizeUrl } = require('../../utils/url');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 页面类型：attraction-景点，hotel-酒店，product-商品
    type: '',
    // 商品ID
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
    
    // 最小日期（今天）
    minDate: '',
    
    // 是否显示房型选择弹窗（酒店）
    showRoomModal: false,
    // 选中的房型
    selectedRoom: null,
    // 入住日期
    checkInDate: '',
    // 退房日期
    checkOutDate: '',
    
    // 是否显示规格选择弹窗（商品）
    showSpecModal: false,
    // 选中的规格（商品）
    selectedSpec: {},
    // 规格选择提示信息
    showSpecTip: false,
    specTipText: '',
    
    // 数量选择器值
    quantity: 1,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('详情页加载', options);
    
    const { type, id } = options;
    if (!type || !id) {
      wx.showToast({
        title: '参数错误',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }

    // 设置最小日期（今天）
    const today = new Date();
    const minDateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    
    this.setData({
      type,
      id: parseInt(id),
      minDate: minDateStr,
    });

    // 设置导航栏标题
    const titles = {
      attraction: '景点详情',
      hotel: '酒店详情',
      product: '商品详情',
    };
    wx.setNavigationBarTitle({
      title: titles[type] || '详情',
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
   * 加载详情数据
   */
  async loadDetail() {
    const { type, id } = this.data;
    
    this.setData({ loading: true });

    try {
      let detail = null;
      
      // 根据类型调用不同的API
      switch (type) {
        case 'attraction':
          detail = await productApi.getAttractionDetail(id);
          break;
        case 'hotel':
          detail = await productApi.getHotelDetail(id);
          break;
        case 'product':
          detail = await productApi.getProductDetail(id);
          break;
        default:
          throw new Error('不支持的类型');
      }

      // 打印详情数据，便于调试
      console.log('========== 商品详情数据 ==========');
      console.log('详情数据:', JSON.stringify(detail, null, 2));
      console.log('商品名称:', detail?.name);
      console.log('商品价格:', detail?.price);
      console.log('商品图片:', detail?.images);
      console.log('商品描述:', detail?.description);
      console.log('================================');
      
      // 处理媒体列表（图片和视频）- 后端已处理OSS签名，这里只处理localhost
      const mediaList = this.processMediaList(detail);
      
      console.log('处理后的媒体列表:', mediaList);
      
      // 处理规格数据（如果是商品且有规格）
      if (type === 'product' && detail.specifications) {
        // 确保规格值是数组格式，并转换为数组格式便于模板遍历
        const processedSpecs = {};
        const specList = []; // 用于模板遍历的数组格式
        Object.keys(detail.specifications).forEach(key => {
          const value = detail.specifications[key];
          let options = [];
          // 如果是数组，直接使用；如果是字符串，转换为数组
          if (Array.isArray(value)) {
            options = value;
          } else if (typeof value === 'string') {
            options = [value];
          }
          processedSpecs[key] = options;
          // 添加到数组格式中
          if (options.length > 0) {
            specList.push({
              key: key,
              value: options
            });
          }
        });
        detail.specifications = processedSpecs;
        detail.specList = specList; // 添加数组格式的规格列表
      }
      
      // 设置详情数据（后端已处理OSS签名）
      this.setData({
        detail,
        mediaList,
        loading: false,
      });
      
      console.log('页面数据已更新，detail:', this.data.detail);
      console.log('页面数据已更新，mediaList:', this.data.mediaList);
    } catch (error) {
      console.error('加载详情失败', error);
      this.setData({ loading: false });
      wx.showToast({
        title: error.message || '加载失败',
        icon: 'none',
      });
    }
  },

  /**
   * 处理媒体列表（图片和视频）
   */
  processMediaList(detail) {
    const mediaList = [];
    
    // 处理图片
    if (detail.images && detail.images.length > 0) {
      detail.images.forEach((img) => {
        mediaList.push({
          type: 'image',
          url: normalizeUrl(img),
        });
      });
    } else if (detail.image) {
      // 单个图片
      mediaList.push({
        type: 'image',
        url: normalizeUrl(detail.image),
      });
    }
    
    // 处理视频
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
    
    // 获取所有图片URL
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

  /**
   * 视频播放事件
   */
  onVideoPlay(e) {
    console.log('视频播放', e);
  },

  /**
   * 视频全屏事件
   */
  onVideoFullscreenChange(e) {
    console.log('视频全屏变化', e.detail);
  },

  /**
   * 标签页切换
   */
  onTabChange(e) {
    const { index } = e.currentTarget.dataset;
    this.setData({
      activeTab: parseInt(index),
    });
  },

  /**
   * 选择房型（酒店）
   */
  onSelectRoom(e) {
    const { room } = e.currentTarget.dataset;
    this.setData({
      selectedRoom: room,
      showRoomModal: true,
    });
  },

  /**
   * 关闭房型选择弹窗
   */
  onCloseRoomModal() {
    this.setData({
      showRoomModal: false,
    });
  },

  /**
   * 选择入住日期
   */
  onCheckInDateChange(e) {
    const checkInDate = e.detail.value;
    const { checkOutDate } = this.data;
    
    // 如果退房日期早于入住日期，清空退房日期
    if (checkOutDate && checkOutDate <= checkInDate) {
      this.setData({
        checkInDate,
        checkOutDate: '',
      });
    } else {
      this.setData({
        checkInDate,
      });
    }
  },

  /**
   * 选择退房日期
   */
  onCheckOutDateChange(e) {
    const checkOutDate = e.detail.value;
    const { checkInDate } = this.data;
    
    // 验证退房日期必须晚于入住日期
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

  /**
   * 减少数量
   */
  onDecreaseQuantity() {
    const { quantity, detail } = this.data;
    if (quantity <= 1) {
      return;
    }
    this.setData({
      quantity: quantity - 1,
    });
  },

  /**
   * 增加数量
   */
  onIncreaseQuantity() {
    const { quantity, detail } = this.data;
    
    // 验证库存
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
   * 加入购物车
   */
  async onAddToCart() {
    // 检查登录状态
    if (!auth.isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再加入购物车',
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
    
    const { type, id, detail, quantity, selectedRoom, selectedSpec, showSpecModal, showRoomModal } = this.data;
    
    // 如果是从弹窗中点击确定按钮，直接执行加入购物车逻辑
    if (showSpecModal || showRoomModal) {
      await this.doAddToCart();
      return;
    }
    
    // 酒店需要选择房型
    if (type === 'hotel') {
      if (!selectedRoom) {
        wx.showToast({
          title: '请选择房型',
          icon: 'none',
        });
        this.setData({ showRoomModal: true });
        return;
      }
    }
    
    // 商品需要选择规格（如果有规格）
    if (type === 'product' && detail && detail.specifications) {
      const specKeys = Object.keys(detail.specifications);
      if (specKeys.length > 0) {
        // 检查是否所有规格都已选择
        let allSpecsSelected = true;
        if (!selectedSpec || Object.keys(selectedSpec).length === 0) {
          allSpecsSelected = false;
        } else {
          // 检查每个规格是否都已选择
          for (let key of specKeys) {
            if (!selectedSpec[key]) {
              allSpecsSelected = false;
              break;
            }
          }
        }
        
        // 如果有规格但未全部选择，弹出规格选择弹窗
        if (!allSpecsSelected) {
          // 默认选中每个规格的第一个选项
          const defaultSelectedSpec = {};
          Object.keys(detail.specifications).forEach(key => {
            const options = detail.specifications[key];
            if (Array.isArray(options) && options.length > 0) {
              defaultSelectedSpec[key] = options[0];
            }
          });
          
          this.setData({ 
            showSpecModal: true,
            selectedSpec: defaultSelectedSpec,
          });
          return;
        }
      }
    }
    
    // 执行加入购物车
    await this.doAddToCart();
  },
  
  /**
   * 执行加入购物车操作
   */
  async doAddToCart() {
    const { type, id, detail, quantity, selectedRoom, selectedSpec } = this.data;
    
    try {
      // 构建购物车数据
      // 根据后端API，商品类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品
      let itemType = '';
      let itemId = id;
      
      if (type === 'attraction') {
        itemType = 'ATTRACTION';
      } else if (type === 'hotel') {
        itemType = 'HOTEL_ROOM';
        if (selectedRoom) {
          itemId = selectedRoom.id; // 酒店使用房型ID
        }
      } else {
        itemType = 'PRODUCT';
      }
      
      const cartData = {
        itemType,
        itemId,
        quantity,
      };
      
      // 调用API添加到购物车（云购物车方案：立即保存到后端数据库）
      await cartApi.addToCart(cartData);
      
      // 确保loading已关闭，延迟显示成功提示
      setTimeout(() => {
        wx.showToast({
          title: '添加购物车成功',
          icon: 'success',
          duration: 2000,
        });
      }, 100);
      
      // 更新全局购物车数量（从后端实时查询）
      const app = getApp();
      if (app && app.updateCartCount) {
        app.updateCartCount();
      }
      
      // 延迟关闭弹窗，确保toast先显示
      if (type === 'hotel') {
        setTimeout(() => {
          this.setData({ 
            showRoomModal: false,
          });
        }, 300);
      } else if (type === 'product') {
        setTimeout(() => {
          this.setData({ 
            showSpecModal: false,
            showSpecTip: false,
            specTipText: '',
          });
        }, 300);
      }
    } catch (error) {
      console.error('加入购物车失败', error);
      // 如果是401错误，request.js已经处理了跳转登录，这里只显示错误提示
      if (error.message && !error.message.includes('未授权') && !error.message.includes('请先登录')) {
        wx.showToast({
          title: error.message || '加入购物车失败',
          icon: 'none',
        });
      }
    }
  },
  
  /**
   * 选择规格（商品）
   */
  onSelectSpec(e) {
    const { specKey, specValue } = e.currentTarget.dataset;
    const { selectedSpec } = this.data;
    
    const newSelectedSpec = {};
    // 复制已有的规格选择
    if (selectedSpec) {
      Object.keys(selectedSpec).forEach(key => {
        newSelectedSpec[key] = selectedSpec[key];
      });
    }
    // 设置当前选择的规格
    newSelectedSpec[specKey] = specValue;
    
    // 显示提示信息
    const tipText = `已选择：${specKey}-${specValue}`;
    this.setData({
      selectedSpec: newSelectedSpec,
      showSpecTip: true,
      specTipText: tipText,
    });
    
    // 5秒后自动隐藏提示
    if (this.specTipTimer) {
      clearTimeout(this.specTipTimer);
    }
    this.specTipTimer = setTimeout(() => {
      this.setData({
        showSpecTip: false,
        specTipText: '',
      });
    }, 5000);
  },
  
  /**
   * 关闭规格选择弹窗
   */
  onCloseSpecModal() {
    // 清除定时器
    if (this.specTipTimer) {
      clearTimeout(this.specTipTimer);
      this.specTipTimer = null;
    }
    
    this.setData({ 
      showSpecModal: false,
      showSpecTip: false,
      specTipText: '',
    });
  },

  /**
   * 立即购买
   */
  async onBuyNow() {
    const { type, id, detail, quantity, selectedRoom, checkInDate, checkOutDate } = this.data;
    
    // 酒店需要选择房型和日期
    if (type === 'hotel') {
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
    }
    
    // 先加入购物车，然后跳转到订单确认页
    try {
      // 构建购物车数据
      let itemType = '';
      let itemId = id;
      
      if (type === 'attraction') {
        itemType = 'ATTRACTION';
      } else if (type === 'hotel') {
        itemType = 'HOTEL_ROOM';
        if (selectedRoom) {
          itemId = selectedRoom.id; // 酒店使用房型ID
        }
      } else {
        itemType = 'PRODUCT';
      }
      
      const cartData = {
        itemType,
        itemId,
        quantity,
      };
      
      const cartItem = await cartApi.addToCart(cartData);
      
      // 跳转到订单确认页
      wx.navigateTo({
        url: `/pages/order/confirm?cartIds=${cartItem.id}`,
      });
    } catch (error) {
      // 如果是未登录错误，不显示错误提示（已经跳转到登录页了）
      if (error.isAuthError) {
        return;
      }
      
      console.error('立即购买失败', error);
      wx.showToast({
        title: error.message || '购买失败',
        icon: 'none',
      });
    }
  },

  /**
   * 联系客服
   */
  onContactService() {
    wx.showToast({
      title: '客服功能开发中',
      icon: 'none',
    });
    // TODO: 实现客服功能
  },

  /**
   * 跳转到首页
   */
  onGoHome() {
    wx.switchTab({
      url: '/pages/home/index',
    });
  },

  /**
   * 跳转到购物车
   */
  onGoCart() {
    wx.switchTab({
      url: '/pages/cart/index',
    });
  },

  /**
   * 分享功能
   */
  onShareAppMessage() {
    const { type, id, detail } = this.data;
    return {
      title: detail?.name || '详情',
      path: `/pages/detail/index?type=${type}&id=${id}`,
      imageUrl: detail?.image || '',
    };
  },

  /**
   * 分享到朋友圈
   */
  onShareTimeline() {
    const { type, id, detail } = this.data;
    return {
      title: detail?.name || '详情',
      query: `type=${type}&id=${id}`,
      imageUrl: detail?.image || '',
    };
  },
});
