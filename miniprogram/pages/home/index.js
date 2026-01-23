/**
 * 首页
 */

const homeApi = require('../../api/home');
const storage = require('../../utils/storage');
const { normalizeUrl } = require('../../utils/url');

// 缓存键名
const CACHE_KEY_HOME = 'home_data';
const CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟缓存

Page({
  /**
   * 页面的初始数据
   */
  data: {
    banners: [], // 轮播图数据
    icons: [], // Icon 图标列表
    recommendAttractions: [], // 推荐景点
    recommendHotels: [], // 推荐酒店
    recommendProducts: [], // 推荐商品（兼容旧数据）
    recommendProductCategories: [], // 推荐商品分类列表（包含分类和商品）
    currentProductCategoryIndex: 0, // 当前选中的商品分类页签索引
    loading: false, // 加载状态
    isPageLoaded: false, // 页面是否已加载
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('首页加载');
    this.loadData().then(() => {
      this.setData({
        isPageLoaded: true,
      });
    });
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 更新购物车数量
    const app = getApp();
    if (app && app.updateCartCount) {
      app.updateCartCount();
    }
    
    // 如果页面已加载过，每次显示时都刷新数据（强制刷新，忽略缓存）
    if (this.data.isPageLoaded) {
      this.loadData(true);
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    console.log('首页渲染完成');
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadData(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 加载数据
   * @param {boolean} forceRefresh 是否强制刷新（忽略缓存）
   */
  async loadData(forceRefresh = false) {
    // 如果正在加载，直接返回
    if (this.data.loading) {
      return Promise.resolve();
    }

    this.setData({
      loading: true,
    });

    try {
      // 如果不是强制刷新，先尝试从缓存获取
      if (!forceRefresh) {
        const cachedData = await this.getCachedData();
        if (cachedData) {
          this.setData({
            banners: cachedData.banners || [],
            icons: cachedData.icons || [],
            recommendAttractions: cachedData.recommendAttractions || [],
            recommendHotels: cachedData.recommendHotels || [],
            recommendProducts: cachedData.recommendProducts || [],
            recommendProductCategories: cachedData.recommendProductCategories || [],
            currentProductCategoryIndex: 0,
            loading: false,
          });
          return Promise.resolve();
        }
      }

      // 调用API获取数据
      const homeData = await homeApi.getHome();
      console.log('首页原始数据:', JSON.stringify(homeData, null, 2));
      
      // 处理数据 - 确保 banners 存在且是数组
      const bannersData = homeData?.banners || [];
      console.log('轮播图原始数据:', bannersData, '数量:', bannersData.length);
      
      const banners = bannersData.map((banner, index) => ({
        id: banner.id || `banner_${index}`,
        type: banner.type || 'image',
        image: normalizeUrl(banner.image || ''),
        video: normalizeUrl(banner.video || ''),
        link: banner.link || '',
        title: banner.title || '',
      }));
      console.log('处理后的轮播图数据:', banners);

      const icons = (homeData?.icons || []).map(icon => ({
        id: icon.id,
        type: icon.type || '',
        relatedId: icon.relatedId || null,
        relatedName: icon.relatedName || '',
        name: icon.name || '',
        icon: normalizeUrl(icon.icon || ''),
        linkUrl: icon.linkUrl || '', // H5链接地址
        categoryId: icon.categoryId || null, // 文章分类ID
      }));
      console.log('处理后的图标数据:', icons);

      const recommendAttractions = (homeData.recommendAttractions || []).map(item => ({
        id: item.id,
        name: item.name || '',
        image: normalizeUrl(item.image || ''),
        coverImage: normalizeUrl(item.image || ''),
        city: item.city || '',
        price: item.price || 0,
        minPrice: item.price || 0,
        productType: 'ATTRACTION',
      }));

      const recommendHotels = (homeData.recommendHotels || []).map(item => ({
        id: item.id,
        name: item.name || '',
        image: normalizeUrl(item.image || ''),
        coverImage: normalizeUrl(item.image || ''),
        city: item.city || '',
        price: item.price || 0,
        minPrice: item.price || 0,
        starLevel: item.starLevel || 0,
        productType: 'HOTEL',
      }));

      const recommendProducts = (homeData.recommendProducts || []).map(item => ({
        id: item.id,
        name: item.name || '',
        image: normalizeUrl(item.image || ''),
        coverImage: normalizeUrl(item.image || ''),
        price: item.price || 0,
        minPrice: item.price || 0,
        originalPrice: item.originalPrice || 0,
        sales: item.sales || 0,
        productType: 'PRODUCT',
      }));

      // 处理推荐商品分类数据（按分类分组）
      const recommendProductCategories = (homeData.recommendProductCategories || []).map(category => ({
        categoryId: category.categoryId,
        categoryName: category.categoryName || '',
        categoryIcon: normalizeUrl(category.categoryIcon || ''),
        products: (category.products || []).map(item => ({
          id: item.id,
          name: item.name || '',
          image: normalizeUrl(item.image || ''),
          coverImage: normalizeUrl(item.image || ''),
          price: item.price || 0,
          minPrice: item.price || 0,
          originalPrice: item.originalPrice || 0,
          sales: item.sales || 0,
          productType: 'PRODUCT',
        })),
      }));

      // 更新页面数据
      this.setData({
        banners,
        icons,
        recommendAttractions,
        recommendHotels,
        recommendProducts,
        recommendProductCategories,
        currentProductCategoryIndex: 0, // 默认选中第一个分类
        loading: false,
      });

      // 缓存数据
      await this.setCachedData({
        banners,
        icons,
        recommendAttractions,
        recommendHotels,
        recommendProducts,
        recommendProductCategories,
      });
    } catch (error) {
      console.error('加载首页数据失败:', error);
      console.error('错误详情:', error.message, error.stack);
      this.setData({
        loading: false,
        banners: [],
        icons: [],
        recommendAttractions: [],
        recommendHotels: [],
        recommendProducts: [],
        recommendProductCategories: [],
        currentProductCategoryIndex: 0,
      });
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
        duration: 2000,
      });
    }
  },

  /**
   * 获取缓存数据
   */
  async getCachedData() {
    try {
      const cached = await storage.getStorage(CACHE_KEY_HOME);
      if (!cached || !cached.data || !cached.timestamp) {
        return null;
      }

      // 检查缓存是否过期
      const now = Date.now();
      if (now - cached.timestamp > CACHE_EXPIRE_TIME) {
        return null;
      }

      return cached.data;
    } catch (error) {
      console.error('获取缓存数据失败:', error);
      return null;
    }
  },

  /**
   * 设置缓存数据
   */
  async setCachedData(data) {
    try {
      await storage.setStorage(CACHE_KEY_HOME, {
        data,
        timestamp: Date.now(),
      });
    } catch (error) {
      console.error('设置缓存数据失败:', error);
    }
  },

  /**
   * 点击搜索框
   */
  onSearchTap() {
    wx.navigateTo({
      url: '/pages/search/index',
      fail: () => {
        // 如果搜索页面不存在，显示提示
        wx.showToast({
          title: '搜索功能开发中',
          icon: 'none',
        });
      },
    });
  },

  /**
   * 轮播图切换事件
   */
  onBannerChange(e) {
    // console.log('轮播图切换:', e.detail);
  },

  /**
   * 轮播图点击事件
   */
  onBannerTap(e) {
    const { banner } = e.detail;
    if (!banner) {
      return;
    }

    // 如果有链接，跳转到链接页面
    if (banner.link) {
      // 这里可以根据link的类型进行不同的跳转处理
      // 暂时不做处理，由Banner组件自己处理图片预览和视频播放
    }
  },

  /**
   * 商品卡片点击事件
   */
  onProductTap(e) {
    const { product, productType } = e.detail;
    // ProductCard组件已经处理了跳转逻辑，这里可以做一些统计或其他处理
    console.log('商品点击:', productType, product);
  },

  /**
   * 查看更多景点
   */
  onViewMoreAttractions() {
    wx.switchTab({
      url: '/pages/category/index?type=attraction',
    });
  },

  /**
   * 查看更多酒店
   */
  onViewMoreHotels() {
    wx.switchTab({
      url: '/pages/category/index?type=hotel',
    });
  },

  /**
   * 切换商品分类页签
   */
  onProductCategoryTabChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      currentProductCategoryIndex: index,
    });
  },

  /**
   * 查看更多商品
   */
  onViewMoreProducts() {
    wx.switchTab({
      url: '/pages/category/index?type=product',
    });
  },

  /**
   * Icon 图标点击事件
   */
  async onIconTap(e) {
    const { icon } = e.currentTarget.dataset;
    if (!icon) {
      return;
    }

    // 根据类型进行跳转
    const { type, relatedId, linkUrl } = icon;
    
    if (type === 'product_category') {
      // 跳转到独立的商品列表页
      const iconName = icon.name || '商品列表';
      wx.navigateTo({
        url: `/pages/product/list?categoryId=${relatedId}&iconName=${encodeURIComponent(iconName)}`,
        fail: (err) => {
          console.error('跳转商品列表失败:', err);
          wx.showToast({
            title: '跳转失败，请重试',
            icon: 'none',
          });
        },
      });
    } else if (type === 'article_category') {
      // 文章分类类型：判断是分类ID还是文章ID
      // 如果 relatedId === categoryId，说明只选择了分类，跳转到文章列表
      // 如果 relatedId !== categoryId，说明选择了具体文章，跳转到文章详情
      const categoryId = icon.categoryId;
      
      // 如果 relatedId 为空，但 categoryId 不为空，使用 categoryId 作为 relatedId（兼容只选择分类的情况）
      const finalRelatedId = relatedId || categoryId;
      
      if (!finalRelatedId) {
        wx.showToast({
          title: '关联ID不能为空',
          icon: 'none',
        });
        return;
      }
      
      // 判断 relatedId 是分类ID还是文章ID
      if (categoryId && finalRelatedId === categoryId) {
        // 只选择了分类，跳转到文章列表（从Icon配置跳转）
        const iconName = icon.name || '文章列表';
        wx.navigateTo({
          url: `/pages/article/list?categoryId=${finalRelatedId}&fromIcon=true&iconName=${encodeURIComponent(iconName)}`,
          fail: (err) => {
            console.error('跳转文章列表失败:', err);
            wx.showToast({
              title: '跳转失败，请重试',
              icon: 'none',
            });
          },
        });
      } else {
        // 选择了具体文章，跳转到文章详情
        wx.navigateTo({
          url: `/pages/article/detail?id=${finalRelatedId}`,
          fail: (err) => {
            console.error('跳转文章详情失败:', err);
            wx.showToast({
              title: '文章不存在',
              icon: 'none',
            });
          },
        });
      }
    } else if (type === 'article') {
      // 跳转到文章详情页
      wx.navigateTo({
        url: `/pages/article/detail?id=${relatedId}`,
        fail: (err) => {
          console.error('跳转文章详情失败:', err);
          wx.showToast({
            title: '文章不存在',
            icon: 'none',
          });
        },
      });
    } else if (type === 'h5_link') {
      // H5 链接类型：跳转到外部链接
      if (linkUrl) {
        // 验证 URL 格式
        if (!linkUrl.startsWith('http://') && !linkUrl.startsWith('https://')) {
          wx.showToast({
            title: '链接地址无效',
            icon: 'none',
          });
          return;
        }
        
        // 方案1：使用 web-view 组件（需要配置业务域名）
        wx.navigateTo({
          url: `/pages/webview/index?url=${encodeURIComponent(linkUrl)}`,
          fail: (err) => {
            console.error('跳转 web-view 失败:', err);
            // 方案2：复制链接到剪贴板（备选方案）
            wx.setClipboardData({
              data: linkUrl,
              success: () => {
                wx.showModal({
                  title: '提示',
                  content: '链接已复制到剪贴板，请在浏览器中打开',
                  showCancel: false,
                });
              },
              fail: () => {
                wx.showToast({
                  title: '链接复制失败',
                  icon: 'none',
                });
              },
            });
          },
        });
      } else {
        wx.showToast({
          title: '链接地址为空',
          icon: 'none',
        });
      }
    } else if (type === 'attraction') {
      // 跳转到景点详情
      wx.navigateTo({
        url: `/pages/detail/index?id=${relatedId}&type=attraction`,
      });
    } else if (type === 'hotel') {
      // 跳转到酒店详情
      wx.navigateTo({
        url: `/pages/detail/index?id=${relatedId}&type=hotel`,
      });
    } else if (type === 'product') {
      // 跳转到商品详情
      wx.navigateTo({
        url: `/pages/detail/index?id=${relatedId}&type=product`,
      });
    } else if (type === 'category_navigation') {
      // 跳转到分类导航页面（tabBar 页面）
      wx.switchTab({
        url: '/pages/category/index',
        fail: (err) => {
          console.error('跳转分类导航失败:', err);
          wx.showToast({
            title: '跳转失败，请重试',
            icon: 'none',
          });
        },
      });
    } else {
      // 默认跳转到分类页面
      wx.switchTab({
        url: '/pages/category/index',
      });
    }
  },
});
