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
    relatedArticles: [], // 相关文章
    isLiked: false, // 是否已点赞
    isFavorited: false, // 是否已收藏
    loading: false, // 加载状态
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

    this.setData({
      articleId,
    });

    // 加载文章详情和相关文章
    this.loadArticleDetail();
    this.loadRelatedArticles();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 刷新文章详情（更新点赞和收藏状态）
    if (this.data.articleId) {
      this.loadArticleDetail();
    }
  },

  /**
   * 加载文章详情
   */
  async loadArticleDetail() {
    if (this.data.loading) {
      return;
    }

    this.setData({
      loading: true,
    });

    try {
      const article = await articleApi.getArticleDetail(this.data.articleId);
      console.log('文章详情:', article);

      // 格式化文章数据
      const formattedArticle = this.formatArticleData(article);

      this.setData({
        article: formattedArticle,
        isLiked: formattedArticle.isLiked || false,
        isFavorited: formattedArticle.isFavorited || false,
        loading: false,
      });
    } catch (error) {
      console.error('加载文章详情失败:', error);
      this.setData({
        loading: false,
      });
    }
  },

  /**
   * 加载相关文章
   */
  async loadRelatedArticles() {
    try {
      const result = await articleApi.getRelatedArticles(this.data.articleId, { limit: 5 });
      const relatedArticles = Array.isArray(result) ? result : (result.list || result.data || []);
      
      // 格式化相关文章数据
      const formattedArticles = relatedArticles.map(article => this.formatArticleData(article));

      this.setData({
        relatedArticles: formattedArticles,
      });
    } catch (error) {
      console.error('加载相关文章失败:', error);
    }
  },

  /**
   * 格式化文章数据
   */
  formatArticleData(article) {
    return {
      ...article,
      publishTimeText: this.formatDateTime(article.publishTime || article.createTime),
      viewCount: article.viewCount || 0,
      likeCount: article.likeCount || 0,
      favoriteCount: article.favoriteCount || 0,
    };
  },

  /**
   * 点赞/取消点赞
   */
  async onLikeTap() {
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

    try {
      if (this.data.isLiked) {
        // 取消点赞
        await articleApi.unlikeArticle(this.data.articleId);
        this.setData({
          isLiked: false,
          'article.likeCount': Math.max(0, (this.data.article.likeCount || 0) - 1),
        });
        wx.showToast({
          title: '已取消点赞',
          icon: 'none',
        });
      } else {
        // 点赞
        await articleApi.likeArticle(this.data.articleId);
        this.setData({
          isLiked: true,
          'article.likeCount': (this.data.article.likeCount || 0) + 1,
        });
        wx.showToast({
          title: '点赞成功',
          icon: 'success',
        });
      }
    } catch (error) {
      console.error('点赞操作失败:', error);
    }
  },

  /**
   * 收藏/取消收藏
   */
  async onFavoriteTap() {
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

    try {
      if (this.data.isFavorited) {
        // 取消收藏
        await articleApi.unfavoriteArticle(this.data.articleId);
        this.setData({
          isFavorited: false,
          'article.favoriteCount': Math.max(0, (this.data.article.favoriteCount || 0) - 1),
        });
        wx.showToast({
          title: '已取消收藏',
          icon: 'none',
        });
      } else {
        // 收藏
        await articleApi.favoriteArticle(this.data.articleId);
        this.setData({
          isFavorited: true,
          'article.favoriteCount': (this.data.article.favoriteCount || 0) + 1,
        });
        wx.showToast({
          title: '收藏成功',
          icon: 'success',
        });
      }
    } catch (error) {
      console.error('收藏操作失败:', error);
    }
  },

  /**
   * 分享文章
   */
  onShareAppMessage() {
    return {
      title: this.data.article ? this.data.article.title : '文章分享',
      path: `/pages/article/detail?id=${this.data.articleId}`,
      imageUrl: this.data.article ? this.data.article.coverImage : '',
    };
  },

  /**
   * 点击相关文章
   */
  onRelatedArticleTap(e) {
    const articleId = e.currentTarget.dataset.id;
    wx.redirectTo({
      url: `/pages/article/detail?id=${articleId}`,
    });
  },

  /**
   * 格式化日期时间
   */
  formatDateTime(dateTime) {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  },
});
