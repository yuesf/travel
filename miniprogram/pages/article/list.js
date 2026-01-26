/**
 * 文章列表页
 */

const articleApi = require('../../api/article');
const auth = require('../../utils/auth');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    articleList: [], // 文章列表
    categoryList: [], // 分类列表（暂时为空，后续可添加API）
    tagList: [], // 标签列表（暂时为空，后续可添加API）
    selectedCategoryId: null, // 选中的分类ID
    selectedTagId: null, // 选中的标签ID
    loading: false, // 加载状态
    hasMore: true, // 是否还有更多数据
    page: 1, // 当前页码
    pageSize: 10, // 每页数量
    fromIcon: false, // 是否从Icon配置跳转过来
  },

  // 保存页面参数，供 onReady 使用
  options: {},

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('文章列表页加载', options);
    
    // 保存页面参数
    this.options = options;
    
    // 优先设置页面标题，避免显示默认的"文章"标题
    // 判断是否从Icon配置跳转过来
    const fromIcon = options.fromIcon === 'true';
    
    // 设置页面标题：优先使用传递的页面标题，其次使用iconName（无论是否标记fromIcon）
    let titleToSet = null;
    if (options.pageTitle) {
      titleToSet = decodeURIComponent(options.pageTitle);
    } else if (options.iconName) {
      titleToSet = decodeURIComponent(options.iconName);
    }
    
    // 立即设置标题，避免显示默认的"文章"
    if (titleToSet) {
      wx.setNavigationBarTitle({
        title: titleToSet,
      });
    }
    
    // 从页面参数获取筛选条件
    const categoryId = options.categoryId;
    const tagId = options.tagId;
    
    if (categoryId) {
      this.setData({
        selectedCategoryId: Number(categoryId),
      });
    }
    
    if (tagId) {
      this.setData({
        selectedTagId: Number(tagId),
      });
    }
    
    // 设置是否从Icon跳转
    this.setData({
      fromIcon: fromIcon,
    });
    
    // 加载文章列表
    this.loadArticleList(true);
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    // 确保标题正确显示（防止JSON默认标题覆盖）
    const options = this.options || {};
    
    // 重新设置标题，确保正确显示
    let titleToSet = null;
    if (options.pageTitle) {
      titleToSet = decodeURIComponent(options.pageTitle);
    } else if (options.iconName) {
      titleToSet = decodeURIComponent(options.iconName);
    }
    
    if (titleToSet) {
      wx.setNavigationBarTitle({
        title: titleToSet,
      });
    }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 刷新文章列表
    this.loadArticleList(true);
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadArticleList(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadArticleList(false);
    }
  },

  /**
   * 加载文章列表
   * @param {boolean} refresh 是否刷新（重置页码）
   */
  async loadArticleList(refresh = false) {
    if (this.data.loading) {
      return;
    }

    const page = refresh ? 1 : this.data.page;
    
    this.setData({
      loading: true,
    });

    try {
      const params = {
        page,
        pageSize: this.data.pageSize,
      };
      
      if (this.data.selectedCategoryId) {
        params.categoryId = this.data.selectedCategoryId;
      }
      
      if (this.data.selectedTagId) {
        params.tagId = this.data.selectedTagId;
      }

      const result = await articleApi.getArticleList(params);
      console.log('文章列表:', result);

      // 处理返回的数据格式
      const list = result.list || result.data || (Array.isArray(result) ? result : []);
      const total = result.total || list.length;
      
      // 格式化文章数据
      const formattedList = list.map(article => this.formatArticleData(article));
      const articleList = refresh ? formattedList : [...this.data.articleList, ...formattedList];
      const hasMore = articleList.length < total;

      this.setData({
        articleList,
        page: page + 1,
        hasMore,
        loading: false,
      });
    } catch (error) {
      console.error('加载文章列表失败:', error);
      this.setData({
        loading: false,
      });
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
    };
  },

  /**
   * 选择分类
   */
  onSelectCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    const selectedCategoryId = categoryId === this.data.selectedCategoryId ? null : categoryId;
    
    this.setData({
      selectedCategoryId,
      articleList: [],
      page: 1,
      hasMore: true,
    });

    // 重新加载文章列表
    this.loadArticleList(true);
  },

  /**
   * 选择标签
   */
  onSelectTag(e) {
    const tagId = e.currentTarget.dataset.id;
    const selectedTagId = tagId === this.data.selectedTagId ? null : tagId;
    
    this.setData({
      selectedTagId,
      articleList: [],
      page: 1,
      hasMore: true,
    });

    // 重新加载文章列表
    this.loadArticleList(true);
  },

  /**
   * 点击文章卡片
   */
  onArticleTap(e) {
    const articleId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/article/detail?id=${articleId}`,
    });
  },

  /**
   * 点击搜索
   */
  onSearchTap() {
    wx.navigateTo({
      url: '/pages/search/index?type=article',
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
