/**
 * 文章详情页
 */

const articleApi = require('../../api/article');
const auth = require('../../utils/auth');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    articleId: null, // 文章ID
    article: null, // 文章详情
    loading: false, // 加载状态
    isInitialized: false, // 是否已初始化加载
    currentImageIndex: 0, // 当前图片索引
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('文章详情页加载');
    
    const articleId = options.id;
    if (!articleId) {
      wx.showToast({
        title: '文章ID不能为空',
        icon: 'none',
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }

    // 重置状态
    this.setData({
      articleId,
      article: null,
      isInitialized: false,
      loading: false,
    });

    // 加载文章详情
    this.loadArticleDetail();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 简化后的页面不需要在 onShow 时做任何操作
  },

  /**
   * 加载文章详情
   */
  async loadArticleDetail() {
    // 检查 articleId 是否存在
    if (!this.data.articleId) {
      console.warn('文章ID不存在，无法加载文章详情');
      return;
    }

    // 防止重复加载
    if (this.data.loading) {
      console.log('文章详情正在加载中，跳过重复请求');
      return;
    }

    this.setData({
      loading: true,
    });

    try {
      const article = await articleApi.getArticleDetail(this.data.articleId);
      console.log('文章详情:', article);

      if (!article) {
        throw new Error('文章数据为空');
      }

      // 格式化文章数据
      const formattedArticle = this.formatArticleData(article);

      this.setData({
        article: formattedArticle,
        loading: false,
        isInitialized: true, // 标记已初始化
      });
    } catch (error) {
      console.error('加载文章详情失败:', error);
      wx.showToast({
        title: '加载文章失败',
        icon: 'none',
        duration: 2000,
      });
      this.setData({
        loading: false,
        isInitialized: true, // 即使失败也标记为已初始化，避免重复尝试
      });
    }
  },


  /**
   * 格式化文章数据
   */
  formatArticleData(article) {
    // 处理文章内容，为图片添加适配样式
    let content = article.content || '';
    if (content && typeof content === 'string') {
      // 为所有 img 标签添加样式，确保图片适配屏幕
      content = content.replace(/<img([^>]*)>/gi, (match, attrs) => {
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
    }

    return {
      ...article,
      content,
    };
  },

  /**
   * 轮播图切换事件
   */
  onSwiperChange(e) {
    const current = e.detail.current;
    this.setData({
      currentImageIndex: current,
    });
  },

  /**
   * 图片点击预览
   */
  onImageTap(e) {
    const index = e.currentTarget.dataset.index;
    const urls = e.currentTarget.dataset.urls || [];
    
    if (urls.length === 0) {
      return;
    }

    wx.previewImage({
      current: urls[index],
      urls: urls,
      fail: (err) => {
        console.error('预览图片失败:', err);
        wx.showToast({
          title: '预览图片失败',
          icon: 'none',
        });
      },
    });
  },

  /**
   * 图片加载错误
   */
  onImageError(e) {
    console.error('图片加载失败:', e);
    // 可以在这里设置默认占位图
  },

  /**
   * 分享文章
   */
  onShareAppMessage() {
    return {
      title: this.data.article ? this.data.article.title : '文章分享',
      path: `/pages/article/detail?id=${this.data.articleId}`,
      imageUrl: this.data.article ? (this.data.article.images && this.data.article.images.length > 0 ? this.data.article.images[0] : this.data.article.coverImage) : '',
    };
  },
});
