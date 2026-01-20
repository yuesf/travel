/**
 * 商品卡片组件
 * 显示商品/景点/酒店信息卡片，支持点击跳转
 */

const constants = require('../../utils/constants');
const cartApi = require('../../api/cart');
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
  },

  /**
   * 组件的方法列表
   */
  methods: {
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
        console.error('添加到购物车失败:', error);
        // 如果是401错误，request.js已经处理了跳转登录，这里只显示错误提示
        if (error.message && !error.message.includes('未授权') && !error.message.includes('请先登录')) {
          wx.showToast({
            title: error.message || '添加失败，请重试',
            icon: 'none',
            duration: 2000,
          });
        }
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
  },
});
