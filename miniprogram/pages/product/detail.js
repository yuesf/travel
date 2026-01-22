/**
 * 商品详情页（Icon跳转专用，纯展示模式）
 */

const productApi = require('../../api/product');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    productId: null, // 商品ID
    product: null, // 商品详情
    loading: false, // 加载状态
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('商品详情页加载', options);
    
    const productId = options.id ? parseInt(options.id) : null;
    if (!productId) {
      wx.showToast({
        title: '商品ID不能为空',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }

    this.setData({
      productId,
    });

    // 加载商品详情
    this.loadProductDetail();
  },

  /**
   * 去除HTML标签，只保留纯文本
   * @param {string} html HTML字符串
   * @returns {string} 纯文本
   */
  stripHtmlTags(html) {
    if (!html || typeof html !== 'string') {
      return '';
    }
    // 去除HTML标签
    let text = html.replace(/<[^>]+>/g, '');
    // 去除HTML实体
    text = text.replace(/&nbsp;/g, ' ');
    text = text.replace(/&amp;/g, '&');
    text = text.replace(/&lt;/g, '<');
    text = text.replace(/&gt;/g, '>');
    text = text.replace(/&quot;/g, '"');
    text = text.replace(/&#39;/g, "'");
    // 去除多余空白字符
    text = text.replace(/\s+/g, ' ').trim();
    return text;
  },

  /**
   * 加载商品详情
   */
  async loadProductDetail() {
    if (this.data.loading) {
      return;
    }

    this.setData({
      loading: true,
    });

    try {
      const product = await productApi.getProductDetail(this.data.productId);
      console.log('商品详情:', product);

      // 获取商品描述并去除HTML标签
      const rawDescription = product.description || product.detail || product.introduction || '';
      const cleanDescription = this.stripHtmlTags(rawDescription);
      
      // 格式化商品数据（只保留图片、标题、简介）
      const formattedProduct = {
        id: product.id,
        name: product.name,
        images: product.images || (product.image ? [product.image] : []) || (product.coverImage ? [product.coverImage] : []),
        description: cleanDescription,
      };

      // 设置页面标题为商品名称
      if (formattedProduct.name) {
        wx.setNavigationBarTitle({
          title: formattedProduct.name,
        });
      }

      this.setData({
        product: formattedProduct,
        loading: false,
      });
    } catch (error) {
      console.error('加载商品详情失败:', error);
      wx.showToast({
        title: '商品不存在或已下架',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      this.setData({
        loading: false,
      });
    }
  },
});
