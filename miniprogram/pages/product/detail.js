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
    isH5Type: false, // 是否为H5类型
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('商品详情页加载', options);
    
    const productId = options.id ? parseInt(options.id) : null;
    const isH5Type = options.isH5Type === 'true' || options.isH5Type === true;
    
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
      isH5Type,
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

      // 获取商品描述
      const rawDescription = product.description || product.detail || product.introduction || '';
      
      // 判断是否为H5类型
      const isH5Type = product.categoryType === 'H5';
      
      // 如果是H5类型，保留HTML并格式化；否则去除HTML标签
      let description;
      if (isH5Type) {
        description = this.formatRichText(rawDescription);
      } else {
        description = this.stripHtmlTags(rawDescription);
      }
      
      // 格式化商品数据（只保留图片、标题、简介）
      const formattedProduct = {
        id: product.id,
        name: product.name,
        images: product.images || (product.image ? [product.image] : []) || (product.coverImage ? [product.coverImage] : []),
        description: description,
        h5Link: product.h5Link || null, // H5链接
      };

      // 设置页面标题为商品名称
      if (formattedProduct.name) {
        wx.setNavigationBarTitle({
          title: formattedProduct.name,
        });
      }

      this.setData({
        product: formattedProduct,
        isH5Type,
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

  /**
   * 点击预约按钮
   */
  onReserveTap() {
    const h5Link = this.data.product?.h5Link;
    if (!h5Link) {
      wx.showToast({
        title: 'H5链接不存在',
        icon: 'none',
      });
      return;
    }
    
    // 跳转到H5页面（使用web-view组件）
    wx.navigateTo({
      url: `/pages/webview/index?url=${encodeURIComponent(h5Link)}`,
      fail: (err) => {
        console.error('跳转H5页面失败:', err);
        // 如果webview页面不存在，尝试使用外部浏览器打开
        wx.showModal({
          title: '提示',
          content: '是否在外部浏览器中打开？',
          success: (res) => {
            if (res.confirm) {
              // 复制链接到剪贴板
              wx.setClipboardData({
                data: h5Link,
                success: () => {
                  wx.showToast({
                    title: '链接已复制',
                    icon: 'success',
                  });
                },
              });
            }
          },
        });
      },
    });
  },
});
