/**
 * 商品列表页（Icon跳转专用）
 */

const categoryApi = require('../../api/category');
const constants = require('../../utils/constants');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    categoryId: null, // 分类ID
    iconName: '', // Icon名称（用于页面标题）
    productList: [], // 商品列表
    loading: false, // 加载状态
    hasMore: true, // 是否还有更多数据
    page: 1, // 当前页码
    pageSize: constants.PAGINATION.DEFAULT_PAGE_SIZE, // 每页数量
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('商品列表页加载', options);
    
    const categoryId = options.categoryId ? parseInt(options.categoryId) : null;
    const iconName = options.iconName ? decodeURIComponent(options.iconName) : '商品列表';
    
    if (!categoryId) {
      wx.showToast({
        title: '分类ID不能为空',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    // 设置页面标题
    wx.setNavigationBarTitle({
      title: iconName,
    });
    
    this.setData({
      categoryId,
      iconName,
    });
    
    // 加载商品列表
    this.loadProductList(true);
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadProductList(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadProductList(false);
    }
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
   * 加载商品列表
   * @param {boolean} refresh 是否刷新（重置页码）
   */
  async loadProductList(refresh = false) {
    if (this.data.loading) {
      return;
    }

    const page = refresh ? 1 : this.data.page;
    
    this.setData({
      loading: true,
    });

    try {
      const params = {
        categoryId: this.data.categoryId,
        page,
        pageSize: this.data.pageSize,
      };
      
      console.log('商品列表查询参数:', params);
      
      // 调用商品列表API
      const response = await categoryApi.getProducts(params);
      
      console.log('商品列表API响应:', response);
      
      // 处理数据
      const list = response.list || response.data || [];
      const total = response.total || list.length;
      
      // 格式化商品数据（只保留需要的字段）
      const formattedList = list.map(product => {
        // 获取商品描述并去除HTML标签
        const rawDescription = product.description || product.summary || product.introduction || '';
        const cleanDescription = this.stripHtmlTags(rawDescription);
        
        return {
          id: product.id,
          name: product.name,
          image: product.image || product.coverImage || '/static/images/default-product.png',
          description: cleanDescription,
        };
      });
      
      const productList = refresh ? formattedList : [...this.data.productList, ...formattedList];
      const hasMore = productList.length < total;

      this.setData({
        productList,
        page: page + 1,
        hasMore,
        loading: false,
      });
    } catch (error) {
      console.error('加载商品列表失败:', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
      });
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 点击商品卡片
   */
  onProductTap(e) {
    const productId = e.currentTarget.dataset.id;
    if (!productId) {
      return;
    }
    
    wx.navigateTo({
      url: `/pages/product/detail?id=${productId}&fromIcon=true`,
      fail: (err) => {
        console.error('跳转商品详情失败:', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none',
        });
      },
    });
  },
});
