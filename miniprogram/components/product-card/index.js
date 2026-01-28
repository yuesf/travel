/**
 * 商品卡片组件
 * 显示商品/景点/酒店信息卡片，支持点击跳转
 */

const constants = require('../../utils/constants');
const cartApi = require('../../api/cart');
const orderApi = require('../../api/order');
const auth = require('../../utils/auth');
const productApi = require('../../api/product');

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 商品数据对象
    product: {
      type: Object,
      value: {},
    },
    // 是否显示价格
    showPrice: {
      type: Boolean,
      value: true,
    },
    // 是否显示评分
    showRating: {
      type: Boolean,
      value: true,
    },
    // 商品类型：ATTRACTION-景点，HOTEL-酒店，PRODUCT-商品
    productType: {
      type: String,
      value: 'PRODUCT',
    },
  },

  /**
   * 组件的初始数据
   */
  data: {
    defaultProductImage: constants.DEFAULT_IMAGES.PRODUCT,
    // 是否显示规格选择弹窗
    showSpecModal: false,
    // 商品详情（包含完整规格信息）
    productDetail: null,
    // 选中的规格
    selectedSpec: {},
    // 数量
    quantity: 1,
    // 规格选择提示信息
    showSpecTip: false,
    specTipText: '',
    // 格式化后的描述（富文本）
    formattedDescription: '',
    // 是否显示景点预订弹窗
    showAttractionModal: false,
    // 使用日期（景点）
    useDate: '',
    // 最小日期（今天）
    minDate: '',
    // 是否显示酒店预订弹窗
    showHotelModal: false,
    // 是否展开房型列表
    showRoomList: false,
    // 选中的房型（酒店）
    selectedRoom: null,
    // 入住日期（酒店）
    checkInDate: '',
    // 退房日期（酒店）
    checkOutDate: '',
    // 酒店详情（包含房型列表）
    hotelDetail: null,
    // 景点详情
    attractionDetail: null,
    // 加载状态
    loading: false,
    // 图片错误映射：productId -> true 表示该商品图片加载失败
    imageErrorMap: {},
  },

  /**
   * 组件生命周期
   */
  lifetimes: {
    attached() {
      // 组件挂载时格式化描述
      this.formatDescription();
      
      // 设置最小日期（今天）
      const today = new Date();
      const minDateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
      this.setData({
        minDate: minDateStr,
      });
      
      // 调试：打印组件属性
      const { product, productType } = this.properties;
      console.log('product-card 组件挂载:', {
        productId: product?.id,
        productName: product?.name,
        productType: productType,
        hasProduct: !!product,
      });
    },
  },

  /**
   * 数据监听器
   */
  observers: {
    'product.description, product.summary, productType': function(description, summary, productType) {
      // 当描述、摘要或商品类型变化时，重新格式化
      this.formatDescription();
    },
    'product': function(product) {
      // 当商品数据变化时，检查是否需要清除错误标记
      if (product && product.id) {
        const imageErrorMap = { ...this.data.imageErrorMap };
        const currentImage = product.image || product.coverImage;
        
        // 如果图片已更新且不是默认图片，清除错误标记（新图片可能加载成功）
        // 但如果当前图片和之前失败的图片不同，说明是新图片，清除错误标记
        if (imageErrorMap[product.id] && currentImage && currentImage !== this.data.defaultProductImage) {
          // 检查图片是否真的改变了（通过比较当前图片和之前失败的图片）
          // 这里简化处理：如果图片存在且不是默认图片，就清除错误标记
          delete imageErrorMap[product.id];
          this.setData({
            imageErrorMap,
          });
        }
      }
    },
  },

  /**
   * 组件的方法列表
   */
  methods: {
    /**
     * 图片加载失败事件
     */
    onImageError(e) {
      const productId = e.currentTarget.dataset.productId;
      const isDefault = e.currentTarget.dataset.isDefault === 'true';
      const { product } = this.properties;
      
      console.log('图片加载失败事件触发:', {
        productId,
        isDefault,
        product: product ? { id: product.id, name: product.name } : null,
        currentImageErrorMap: this.data.imageErrorMap,
      });
      
      // 如果当前已经是默认图片，不再处理（避免无限循环）
      if (isDefault) {
        console.log('当前已是默认图片，跳过处理（避免无限循环）');
        return;
      }
      
      // 使用 product.id 或 productId，优先使用 productId（从 dataset 获取）
      const finalProductId = productId || (product && product.id);
      
      if (!finalProductId || !product) {
        console.warn('商品ID或商品数据不存在，无法处理图片错误');
        return;
      }
      
      // 如果已经标记为错误，不再处理（避免重复触发）
      if (this.data.imageErrorMap[finalProductId]) {
        console.log('该商品图片错误已标记，跳过处理');
        return;
      }
      
      const currentImage = product.image || product.coverImage;
      
      // 如果已经是默认图片，不再处理（避免无限循环）
      if (currentImage === this.data.defaultProductImage) {
        console.log('当前已是默认图片，跳过处理');
        return;
      }
      
      console.error('商品图片加载失败，切换到默认图片:', {
        productId: finalProductId,
        productName: product.name,
        failedImage: currentImage,
        defaultImage: this.data.defaultProductImage,
      });
      
      // 标记该商品图片加载失败
      const imageErrorMap = { ...this.data.imageErrorMap };
      imageErrorMap[finalProductId] = true;
      
      console.log('准备更新 imageErrorMap:', {
        before: this.data.imageErrorMap,
        after: imageErrorMap,
        finalProductId,
      });
      
      this.setData({
        imageErrorMap,
      }, () => {
        console.log('已更新 imageErrorMap，当前值:', this.data.imageErrorMap);
        console.log('当前 product.id:', product.id);
        console.log('imageErrorMap[product.id]:', this.data.imageErrorMap[product.id]);
      });
    },

    /**
     * 规格弹窗中的图片加载失败事件
     */
    onSpecImageError(e) {
      const type = e.currentTarget.dataset.type;
      const { product, productDetail } = this.properties;
      
      console.error('规格弹窗图片加载失败:', {
        type,
        productId: product?.id,
        productName: product?.name,
      });
      
      // 如果是商品详情图片失败，更新productDetail
      if (type === 'detail' && productDetail && productDetail.images && productDetail.images.length > 0) {
        const currentImage = productDetail.images[0];
        // 如果已经是默认图片，不再处理（避免无限循环）
        if (currentImage === this.data.defaultProductImage) {
          return;
        }
        productDetail.images[0] = this.data.defaultProductImage;
        this.setData({
          productDetail: { ...productDetail }, // 创建新对象以触发更新
        });
      }
      // 如果是商品图片失败，更新product
      else if (type === 'product' && product) {
        const currentImage = product.image || product.coverImage;
        // 如果已经是默认图片，不再处理（避免无限循环）
        if (currentImage === this.data.defaultProductImage) {
          return;
        }
        product.image = this.data.defaultProductImage;
        product.coverImage = this.data.defaultProductImage;
        this.setData({
          product: { ...product }, // 创建新对象以触发更新
        });
      }
    },

    /**
     * 点击卡片
     */
    onCardTap() {
      const { product, productType } = this.properties;
      
      if (!product || !product.id) {
        return;
      }

      // 触发点击事件
      this.triggerEvent('tap', {
        product,
        productType,
      });

      // 根据商品类型跳转到对应的详情页
      let url = '';
      switch (productType) {
        case 'ATTRACTION':
          url = `/pages/detail/index?type=attraction&id=${product.id}`;
          break;
        case 'HOTEL':
          url = `/pages/detail/index?type=hotel&id=${product.id}`;
          break;
        case 'PRODUCT':
          url = `/pages/detail/index?type=product&id=${product.id}`;
          break;
        default:
          url = `/pages/detail/index?type=product&id=${product.id}`;
      }

      if (url) {
        wx.navigateTo({
          url,
        });
      }
    },

    /**
     * 立即预订按钮触摸开始（阻止事件冒泡）
     */
    onBookNowTouchStart(e) {
      console.log('onBookNowTouchStart 被调用', e);
      // 阻止事件冒泡到父元素
      if (e && typeof e.stopPropagation === 'function') {
        e.stopPropagation();
      }
    },
    
    /**
     * 立即预订按钮触摸结束（作为备用方案，如果 tap 事件不触发）
     */
    onBookNowTouchEnd(e) {
      console.log('onBookNowTouchEnd 被调用', e);
      // 如果 tap 事件没有触发，使用 touchEnd 作为备用
      // 延迟一点时间，让 tap 事件有机会触发
      setTimeout(() => {
        // 检查是否已经有 tap 事件处理了
        if (!this._bookNowHandled) {
          console.log('tap 事件未触发，使用 touchEnd 作为备用');
          this._bookNowHandled = true;
          this.onBookNow(e);
          // 重置标志，以便下次点击
          setTimeout(() => {
            this._bookNowHandled = false;
          }, 500);
        }
      }, 100);
    },

    /**
     * 立即预订（景点和酒店）
     */
    async onBookNow(e) {
      console.log('========== onBookNow 被调用 ==========');
      console.log('事件对象:', e);
      console.log('事件类型:', e?.type);
      console.log('当前时间:', new Date().toISOString());
      
      // 标记已处理，避免 touchEnd 重复调用
      this._bookNowHandled = true;
      
      // 阻止事件冒泡
      if (e) {
        if (typeof e.stopPropagation === 'function') {
          e.stopPropagation();
          console.log('已调用 stopPropagation');
        }
        // 阻止默认行为
        if (typeof e.preventDefault === 'function') {
          e.preventDefault();
          console.log('已调用 preventDefault');
        }
      }
      
      const { product, productType } = this.properties;
      
      console.log('组件属性:', { 
        product: product ? { id: product.id, name: product.name } : null,
        productType: productType,
        hasProduct: !!product,
        hasProductId: !!(product && product.id),
      });
      
      // 从 dataset 获取数据（备用）
      const datasetProductId = e?.currentTarget?.dataset?.productId || e?.target?.dataset?.productId;
      const datasetProductType = e?.currentTarget?.dataset?.productType || e?.target?.dataset?.productType;
      console.log('Dataset 数据:', { datasetProductId, datasetProductType });
      
      if (!product || !product.id) {
        console.warn('商品数据无效:', product);
        wx.showToast({
          title: '商品信息无效',
          icon: 'none',
        });
        // 重置标志
        setTimeout(() => {
          this._bookNowHandled = false;
        }, 500);
        return;
      }
      
      // 检查登录状态
      const isLoggedIn = auth.isLoggedIn();
      console.log('登录状态:', isLoggedIn);
      
      if (!isLoggedIn) {
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
        // 重置标志
        setTimeout(() => {
          this._bookNowHandled = false;
        }, 500);
        return;
      }

      // 根据类型处理预订
      try {
        console.log('开始处理预订，类型:', productType);
        if (productType === 'ATTRACTION') {
          console.log('处理景点预订');
          await this.handleAttractionBooking();
        } else if (productType === 'HOTEL') {
          console.log('处理酒店预订');
          await this.handleHotelBooking();
        } else {
          console.warn('不支持的商品类型:', productType);
          wx.showToast({
            title: '不支持的商品类型',
            icon: 'none',
          });
        }
      } catch (error) {
        console.error('预订处理失败:', error);
        console.error('错误堆栈:', error.stack);
        wx.showToast({
          title: error.message || '预订失败',
          icon: 'none',
        });
      } finally {
        // 重置标志
        setTimeout(() => {
          this._bookNowHandled = false;
        }, 500);
      }
      console.log('========== onBookNow 结束 ==========');
    },

    /**
     * 处理景点预订
     */
    async handleAttractionBooking() {
      const { product } = this.properties;
      
      try {
        this.setData({ loading: true });
        
        // 获取景点详情
        const detail = await productApi.getAttractionDetail(product.id);
        
        this.setData({
          attractionDetail: detail,
          showAttractionModal: true,
          useDate: '',
          quantity: 1,
          loading: false,
        });
      } catch (error) {
        console.error('获取景点详情失败:', error);
        this.setData({ loading: false });
        wx.showToast({
          title: error.message || '获取景点信息失败',
          icon: 'none',
        });
      }
    },

    /**
     * 处理酒店预订
     */
    async handleHotelBooking() {
      const { product } = this.properties;
      
      try {
        this.setData({ loading: true });
        
        // 获取酒店详情（包含房型列表）
        const detail = await productApi.getHotelDetail(product.id);
        
        this.setData({
          hotelDetail: detail,
          showHotelModal: true,
          showRoomList: false,
          selectedRoom: null,
          checkInDate: '',
          checkOutDate: '',
          quantity: 1,
          loading: false,
        });
      } catch (error) {
        console.error('获取酒店详情失败:', error);
        this.setData({ loading: false });
        wx.showToast({
          title: error.message || '获取酒店信息失败',
          icon: 'none',
        });
      }
    },

    /**
     * 关闭景点预订弹窗
     */
    onCloseAttractionModal() {
      this.setData({
        showAttractionModal: false,
      });
    },

    /**
     * 选择使用日期（景点）
     */
    onUseDateChange(e) {
      const useDate = e.detail.value;
      this.setData({
        useDate,
      });
    },

    /**
     * 关闭酒店预订弹窗
     */
    onCloseHotelModal() {
      this.setData({
        showHotelModal: false,
        showRoomList: false,
      });
    },

    /**
     * 切换房型列表显示/隐藏
     */
    onToggleRoomList() {
      this.setData({
        showRoomList: !this.data.showRoomList,
      });
    },

    /**
     * 选择房型（酒店）
     */
    onSelectRoom(e) {
      const { room } = e.currentTarget.dataset;
      this.setData({
        selectedRoom: room,
        showRoomList: false, // 选择后自动收起列表
      });
    },

    /**
     * 选择入住日期（酒店）
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
     * 选择退房日期（酒店）
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
     * 确认景点预订（创建订单）
     */
    async onConfirmAttractionBooking() {
      const { product } = this.properties;
      const { useDate, quantity, attractionDetail } = this.data;
      
      // 验证使用日期
      if (!useDate) {
        wx.showToast({
          title: '请选择使用日期',
          icon: 'none',
        });
        return;
      }
      
      try {
        // 获取用户信息作为联系人信息
        const userInfo = auth.getUserInfo();
        const contactName = userInfo?.nickname || userInfo?.name || '微信用户';
        const contactPhone = userInfo?.phone || '';
        
        // 如果手机号为空，提示用户填写
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
        
        // 构建订单项
        const orderItem = {
          itemType: 'ATTRACTION',
          itemId: product.id,
          quantity: quantity,
          useDate: useDate,
        };
        
        // 构建订单数据
        const orderData = {
          orderType: 'ATTRACTION',
          items: [orderItem],
          contactName: contactName,
          contactPhone: contactPhone,
        };
        
        // 显示加载提示
        wx.showLoading({
          title: '创建订单中...',
          mask: true,
        });
        
        // 创建订单
        const order = await orderApi.createOrder(orderData);
        console.log('订单创建成功:', order);
        
        wx.hideLoading();
        
        // 关闭弹窗
        this.setData({ 
          showAttractionModal: false,
        });
        
        // 跳转到订单详情页
        wx.navigateTo({
          url: `/pages/order/detail?id=${order.id}`,
        });
      } catch (error) {
        wx.hideLoading();
        // 如果是未登录错误，不显示错误提示（已经跳转到登录页了）
        if (error.isAuthError) {
          return;
        }
        
        console.error('创建订单失败', error);
        wx.showToast({
          title: error.message || '创建订单失败',
          icon: 'none',
        });
      }
    },

    /**
     * 确认酒店预订（创建订单）
     */
    async onConfirmHotelBooking() {
      const { product } = this.properties;
      const { selectedRoom, checkInDate, checkOutDate, quantity } = this.data;
      
      // 验证房型
      if (!selectedRoom) {
        // 自动展开房型列表，方便用户选择
        this.setData({
          showRoomList: true,
        });
        wx.showToast({
          title: '请选择房型',
          icon: 'none',
          duration: 2000,
        });
        return;
      }
      
      // 验证日期
      if (!checkInDate || !checkOutDate) {
        wx.showToast({
          title: '请选择入住和退房日期',
          icon: 'none',
        });
        return;
      }
      
      try {
        // 获取用户信息作为联系人信息
        const userInfo = auth.getUserInfo();
        const contactName = userInfo?.nickname || userInfo?.name || '微信用户';
        const contactPhone = userInfo?.phone || '';
        
        // 如果手机号为空，提示用户填写
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
        
        // 构建订单项
        const orderItem = {
          itemType: 'HOTEL_ROOM',
          itemId: selectedRoom.id,
          quantity: quantity,
          checkInDate: checkInDate,
          checkOutDate: checkOutDate,
        };
        
        // 构建订单数据
        const orderData = {
          orderType: 'HOTEL',
          items: [orderItem],
          contactName: contactName,
          contactPhone: contactPhone,
        };
        
        // 显示加载提示
        wx.showLoading({
          title: '创建订单中...',
          mask: true,
        });
        
        // 创建订单
        const order = await orderApi.createOrder(orderData);
        console.log('订单创建成功:', order);
        
        wx.hideLoading();
        
        // 关闭弹窗
        this.setData({ 
          showHotelModal: false,
        });
        
        // 跳转到订单详情页
        wx.navigateTo({
          url: `/pages/order/detail?id=${order.id}`,
        });
      } catch (error) {
        wx.hideLoading();
        // 如果是未登录错误，不显示错误提示（已经跳转到登录页了）
        if (error.isAuthError) {
          return;
        }
        
        console.error('创建订单失败', error);
        wx.showToast({
          title: error.message || '创建订单失败',
          icon: 'none',
        });
      }
    },

    /**
     * 格式化价格
     */
    formatPrice(price) {
      if (!price && price !== 0) {
        return '价格面议';
      }
      return `¥${price.toFixed(2)}`;
    },

    /**
     * 格式化评分
     */
    formatRating(rating) {
      if (!rating && rating !== 0) {
        return '暂无评分';
      }
      return rating.toFixed(1);
    },

    /**
     * 添加到购物车
     */
    async onAddToCart(e) {
      // 阻止事件冒泡（如果事件对象有该方法）
      if (e && typeof e.stopPropagation === 'function') {
        e.stopPropagation();
      }
      
      const { product } = this.properties;
      const { showSpecModal, productDetail, selectedSpec } = this.data;
      
      // 如果是从弹窗中点击确定按钮，直接执行加入购物车逻辑
      if (showSpecModal) {
        await this.doAddToCart();
        return;
      }
      
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
      
      if (!product || !product.id) {
        return;
      }
      
      // 检查商品是否有规格
      let hasSpecifications = false;
      let detail = productDetail || product;
      
      // 如果商品数据中没有规格信息，需要获取商品详情
      if (!detail.specifications || Object.keys(detail.specifications || {}).length === 0) {
        try {
          detail = await productApi.getProductDetail(product.id);
          // 处理规格数据
          if (detail.specifications) {
            const processedSpecs = {};
            const specList = []; // 用于模板遍历的数组格式
            Object.keys(detail.specifications).forEach(key => {
              const value = detail.specifications[key];
              let options = [];
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
            hasSpecifications = Object.keys(processedSpecs).length > 0;
          }
        } catch (error) {
          console.error('获取商品详情失败:', error);
        }
      } else {
        // 处理规格数据
        const processedSpecs = {};
        Object.keys(detail.specifications).forEach(key => {
          const value = detail.specifications[key];
          if (Array.isArray(value)) {
            processedSpecs[key] = value;
          } else if (typeof value === 'string') {
            processedSpecs[key] = [value];
          } else {
            processedSpecs[key] = [];
          }
        });
        detail.specifications = processedSpecs;
        hasSpecifications = Object.keys(processedSpecs).length > 0;
      }
      
      // 如果有规格，弹出规格选择弹窗
      if (hasSpecifications) {
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
          productDetail: detail,
          selectedSpec: defaultSelectedSpec,
          quantity: 1,
        });
        return;
      }
      
      // 没有规格，直接添加到购物车
      await this.doAddToCart();
    },

    /**
     * 执行加入购物车操作
     */
    async doAddToCart() {
      const { product } = this.properties;
      const { quantity = 1, selectedSpec, productDetail } = this.data;
      
      try {
        // 构建购物车数据（商品类型）
        const itemType = 'PRODUCT';
        
        const cartData = {
          itemType,
          itemId: product.id,
          quantity: quantity || 1,
        };
        
        // 调用API添加到购物车
        await cartApi.addToCart(cartData);
        
        // 确保loading已关闭，延迟显示成功提示
        setTimeout(() => {
          wx.showToast({
            title: '添加购物车成功',
            icon: 'success',
            duration: 2000,
          });
        }, 100);
        
        // 更新全局购物车数量
        const app = getApp();
        if (app && app.updateCartCount) {
          app.updateCartCount();
        }
        
        // 延迟关闭弹窗（如果打开了），确保toast先显示
        if (this.data.showSpecModal) {
          setTimeout(() => {
            this.setData({
              showSpecModal: false,
              showSpecTip: false,
              specTipText: '',
            });
          }, 300);
        }
        
        // 触发添加到购物车事件，供父组件监听
        this.triggerEvent('addtocart', {
          product,
          productType: this.properties.productType,
          selectedSpec,
        });
      } catch (error) {
        // 如果是未登录错误，不显示错误提示（已经跳转到登录页了）
        if (error.isAuthError) {
          return;
        }
        
        console.error('添加到购物车失败:', error);
        wx.showToast({
          title: error.message || '添加失败，请重试',
          icon: 'none',
          duration: 2000,
        });
      }
    },

    /**
     * 选择规格
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
     * 增加数量
     */
    onIncreaseQuantity() {
      const { quantity, productDetail } = this.data;
      
      // 验证库存
      if (productDetail && productDetail.stock !== undefined && quantity >= productDetail.stock) {
        wx.showToast({
          title: `库存不足，仅剩${productDetail.stock}件`,
          icon: 'none',
        });
        return;
      }
      
      this.setData({
        quantity: quantity + 1,
      });
    },

    /**
     * 减少数量
     */
    onDecreaseQuantity() {
      const { quantity } = this.data;
      if (quantity > 1) {
        this.setData({
          quantity: quantity - 1,
        });
      }
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
     * 格式化描述内容（富文本）
     * 仅商品类型需要格式化描述，酒店和景点在列表页不显示描述
     */
    formatDescription() {
      const { product, productType } = this.properties;
      
      // 仅商品类型需要格式化描述
      if (productType !== 'PRODUCT') {
        this.setData({
          formattedDescription: '',
        });
        return;
      }
      
      const description = product?.description || product?.summary || '';
      
      if (!description) {
        this.setData({
          formattedDescription: '',
        });
        return;
      }

      // 格式化富文本，为图片添加适配样式
      const formatted = this.formatRichText(description);
      
      this.setData({
        formattedDescription: formatted,
      });
    },

    /**
     * 格式化富文本内容，为图片添加适配样式
     * @param {string} html HTML字符串
     * @returns {string} 格式化后的HTML
     */
    formatRichText(html) {
      if (!html || typeof html !== 'string') {
        return '';
      }
      
      // 为所有 img 标签添加样式，确保图片适配屏幕
      let content = html.replace(/<img([^>]*)>/gi, (match, attrs) => {
        // 检查是否已有 style 属性（支持单引号和双引号）
        const hasStyle = /style\s*=\s*["']/i.test(attrs);
        
        if (hasStyle) {
          // 如果已有 style，添加或更新 max-width
          return match.replace(/style\s*=\s*["']([^"']*)["']/i, (styleMatch, styleValue) => {
            // 检查是否已有 max-width
            if (!/max-width\s*:/i.test(styleValue)) {
              // 移除末尾的分号（如果有），然后添加样式
              const cleanStyle = styleValue.trim().replace(/;?\s*$/, '');
              const quote = styleMatch.includes("'") ? "'" : '"';
              return `style=${quote}${cleanStyle}; max-width: 100%; height: auto; display: block;${quote}`;
            }
            return styleMatch;
          });
        } else {
          // 如果没有 style，添加 style 属性
          return `<img${attrs} style="max-width: 100%; height: auto; display: block;">`;
        }
      });
      
      return content;
    },
  },
});
