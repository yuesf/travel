/**
 * 订单确认页
 */

const orderApi = require('../../api/order');
const cartApi = require('../../api/cart');
const couponApi = require('../../api/coupon');
const addressApi = require('../../api/address');
const auth = require('../../utils/auth');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    cartIds: [], // 购物车ID列表
    cartItems: [], // 购物车商品列表
    couponList: [], // 可用优惠券列表
    selectedCoupon: null, // 选中的优惠券
    selectedAddress: null, // 选中的收货地址
    contactName: '', // 联系人姓名
    contactPhone: '', // 联系电话
    remark: '', // 备注
    totalAmount: 0, // 订单总金额
    discountAmount: 0, // 优惠金额
    shippingFee: 0, // 运费
    payAmount: 0, // 实付金额
    loading: false, // 加载状态
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('订单确认页加载');
    
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

    // 获取购物车ID列表
    const cartIds = options.cartIds ? options.cartIds.split(',') : [];
    if (cartIds.length === 0) {
      wx.showToast({
        title: '请选择要结算的商品',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }

    this.setData({
      cartIds,
    });

    // 并行加载购物车商品、优惠券和收货地址，提升页面加载速度
    this.loadInitialData();
  },

  /**
   * 并行加载初始数据
   * 优化：将无依赖关系的接口并行执行，减少页面加载时间
   */
  async loadInitialData() {
    this.setData({
      loading: true,
    });

    try {
      // 并行执行购物车商品和收货地址的加载（这两个接口无依赖关系）
      const [cartResult, addressResult] = await Promise.allSettled([
        this.loadCartItems(),
        this.loadAddress(),
      ]);

      // 处理购物车加载结果
      if (cartResult.status === 'rejected') {
        console.error('加载购物车商品失败:', cartResult.reason);
      }

      // 处理地址加载结果
      if (addressResult.status === 'rejected') {
        console.error('加载收货地址失败:', addressResult.reason);
      }

      // 购物车加载完成后，再加载优惠券（因为优惠券需要根据订单金额过滤）
      // 注意：这里仍然可以并行，因为 loadCouponList 内部会使用 this.data.totalAmount
      // 但为了确保 totalAmount 已设置，我们在购物车加载完成后再调用
      await this.loadCouponList();
    } catch (error) {
      console.error('加载初始数据失败:', error);
    } finally {
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 加载购物车商品
   * 优化：不在这里显示错误提示，由调用方统一处理
   */
  async loadCartItems() {
    const cartList = await cartApi.getCartList();
    console.log('订单确认页 - 购物车列表:', cartList);
    console.log('订单确认页 - 需要加载的cartIds:', this.data.cartIds);
    
    // 将 itemDetail 映射为 productDetail，保持与购物车页面一致
    const cartItems = cartList
      .filter(item => this.data.cartIds.includes(String(item.id)))
      .map(item => ({
        ...item,
        productDetail: item.itemDetail, // 将 itemDetail 映射为 productDetail
      }));
    
    console.log('订单确认页 - 过滤后的购物车商品:', cartItems);
    
    if (cartItems.length === 0) {
      wx.showToast({
        title: '购物车商品不存在',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      throw new Error('购物车商品不存在');
    }

    // 计算订单总金额
    // 优先使用 itemPrice，如果没有则从 productDetail 中获取
    const totalAmount = cartItems.reduce((sum, item) => {
      let price = 0;
      if (item.itemPrice) {
        // 使用后端返回的 itemPrice
        price = Number(item.itemPrice);
      } else if (item.productDetail) {
        // 从商品详情中获取价格
        price = Number(item.productDetail.price || item.productDetail.minPrice || item.productDetail.ticketPrice || 0);
      }
      return sum + price * item.quantity;
    }, 0);

    console.log('订单确认页 - 订单总金额:', totalAmount);

    this.setData({
      cartItems,
      totalAmount,
    });

    // 重新计算金额
    this.calculateAmount();
  },

  /**
   * 加载优惠券列表
   * 注意：此方法依赖于 totalAmount，应在 loadCartItems 完成后调用
   */
  async loadCouponList() {
    try {
      const result = await couponApi.getCouponList({ status: 0 }); // 0-未使用
      const couponList = result.list || result.data || result;
      
      // 过滤可用优惠券（根据订单金额）
      // 如果 totalAmount 还未设置，先获取所有优惠券，后续再过滤
      const totalAmount = this.data.totalAmount || 0;
      const availableCoupons = couponList.filter(coupon => {
        if (!coupon.coupon) return false;
        const minAmount = coupon.coupon.minAmount || 0;
        return totalAmount >= minAmount;
      });

      this.setData({
        couponList: availableCoupons,
      });
    } catch (error) {
      console.error('加载优惠券列表失败:', error);
      // 优惠券加载失败不影响页面使用，只记录错误
    }
  },

  /**
   * 选择优惠券
   */
  onSelectCoupon() {
    if (this.data.couponList.length === 0) {
      wx.showToast({
        title: '暂无可用的优惠券',
        icon: 'none',
      });
      return;
    }

    // 显示优惠券选择弹窗
    const couponNames = this.data.couponList.map(coupon => {
      const couponInfo = coupon.coupon || {};
      const amount = couponInfo.amount || 0;
      const minAmount = couponInfo.minAmount || 0;
      return `${couponInfo.name} (满${minAmount}减${amount})`;
    });

    wx.showActionSheet({
      itemList: ['不使用优惠券', ...couponNames],
      success: (res) => {
        if (res.tapIndex === 0) {
          this.setData({
            selectedCoupon: null,
          });
        } else {
          this.setData({
            selectedCoupon: this.data.couponList[res.tapIndex - 1],
          });
        }
        this.calculateAmount();
      },
    });
  },

  /**
   * 计算订单金额
   */
  calculateAmount() {
    let discountAmount = 0;
    
    if (this.data.selectedCoupon && this.data.selectedCoupon.coupon) {
      const coupon = this.data.selectedCoupon.coupon;
      const amount = coupon.amount || 0;
      discountAmount = amount;
      
      // 确保优惠金额不超过订单总金额
      if (discountAmount > this.data.totalAmount) {
        discountAmount = this.data.totalAmount;
      }
    }

    const payAmount = this.data.totalAmount - discountAmount + this.data.shippingFee;
    
    this.setData({
      discountAmount,
      payAmount: payAmount > 0 ? payAmount : 0,
    });
  },


  /**
   * 输入联系人姓名
   */
  onContactNameInput(e) {
    this.setData({
      contactName: e.detail.value,
    });
  },

  /**
   * 输入联系电话
   */
  onContactPhoneInput(e) {
    this.setData({
      contactPhone: e.detail.value,
    });
  },

  /**
   * 输入备注
   */
  onRemarkInput(e) {
    this.setData({
      remark: e.detail.value,
    });
  },

  /**
   * 加载收货地址
   * 优化：不在这里显示错误提示，由调用方统一处理
   */
  async loadAddress() {
    const addressList = await addressApi.getAddressList();
    // 查找默认地址，如果没有则使用第一个
    const defaultAddress = addressList.find(addr => addr.isDefault === 1) || addressList[0];
    if (defaultAddress) {
      this.setData({
        selectedAddress: defaultAddress,
        contactName: defaultAddress.receiverName,
        contactPhone: defaultAddress.receiverPhone,
      });
    }
  },

  /**
   * 选择收货地址
   */
  onSelectAddress() {
    // 如果没有地址，直接跳转到添加地址页面
    if (!this.data.selectedAddress) {
      wx.navigateTo({
        url: '/pages/mine/address-edit',
      });
    } else {
      // 有地址时，跳转到地址选择页面
      wx.navigateTo({
        url: '/pages/mine/address?select=true',
      });
    }
  },

  /**
   * 提交订单
   */
  async onSubmitOrder() {
    // 验证收货地址（商品类订单需要收货地址）
    const firstItem = this.data.cartItems[0];
    const isProductOrder = firstItem && firstItem.itemType === 'PRODUCT';
    
    if (isProductOrder && !this.data.selectedAddress) {
      wx.showToast({
        title: '请选择收货地址',
        icon: 'none',
      });
      setTimeout(() => {
        this.onSelectAddress();
      }, 1500);
      return;
    }

    // 验证必填项
    if (!this.data.contactName || !this.data.contactName.trim()) {
      wx.showToast({
        title: '请输入联系人姓名',
        icon: 'none',
      });
      return;
    }

    if (!this.data.contactPhone || !this.data.contactPhone.trim()) {
      wx.showToast({
        title: '请输入联系电话',
        icon: 'none',
      });
      return;
    }

    // 验证手机号格式
    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(this.data.contactPhone)) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none',
      });
      return;
    }

    if (this.data.loading) {
      return;
    }

    this.setData({
      loading: true,
    });

    try {
      // 确定订单类型（取第一个商品的类型）
      const firstItem = this.data.cartItems[0];
      let orderType = 'PRODUCT';
      if (firstItem.itemType === 'ATTRACTION') {
        orderType = 'ATTRACTION';
      } else if (firstItem.itemType === 'HOTEL_ROOM') {
        orderType = 'HOTEL';
      }

      // 构建订单数据
      const orderData = {
        orderType,
        cartIds: this.data.cartIds.map(id => Number(id)),
        couponId: this.data.selectedCoupon ? this.data.selectedCoupon.id : null,
        addressId: this.data.selectedAddress ? this.data.selectedAddress.id : null,
        contactName: this.data.contactName.trim(),
        contactPhone: this.data.contactPhone.trim(),
        remark: this.data.remark ? this.data.remark.trim() : '',
      };

      // 创建订单
      const order = await orderApi.createOrder(orderData);
      console.log('订单创建成功:', order);

      // 调用支付
      await this.payOrder(order.id);

    } catch (error) {
      console.error('创建订单失败:', error);
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 支付订单
   */
  async payOrder(orderId) {
    try {
      // 调用支付接口
      const paymentParams = await orderApi.payOrder(orderId, {
        payType: 'WECHAT',
      });

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
          this.setData({
            loading: false,
          });
          
          if (err.errMsg && err.errMsg.includes('cancel')) {
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
          
          // 支付失败也跳转到订单详情页
          setTimeout(() => {
            wx.redirectTo({
              url: `/pages/order/detail?id=${orderId}`,
            });
          }, 1500);
        },
      });
    } catch (error) {
      console.error('支付失败:', error);
      this.setData({
        loading: false,
      });
      wx.showToast({
        title: '支付失败',
        icon: 'none',
      });
      
      // 支付接口调用失败也跳转到订单详情页
      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/order/detail?id=${orderId}`,
        });
      }, 1500);
    }
  },
});
