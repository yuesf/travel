/**
 * 搜索页面
 */

const homeApi = require('../../api/home');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    keyword: '', // 搜索关键词
    searchHistory: [], // 搜索历史
    searchResults: [], // 搜索结果
    loading: false,
    hasSearched: false, // 是否已搜索
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 加载搜索历史
    this.loadSearchHistory();
    
    // 如果有传入关键词，直接搜索
    if (options.keyword) {
      this.setData({
        keyword: options.keyword,
      });
      this.doSearch(options.keyword);
    }
  },

  /**
   * 加载搜索历史
   */
  loadSearchHistory() {
    const storage = require('../../utils/storage');
    const history = storage.getSearchHistory();
    this.setData({
      searchHistory: history || [],
    });
  },

  /**
   * 输入搜索关键词
   */
  onInput(e) {
    this.setData({
      keyword: e.detail.value,
    });
  },

  /**
   * 确认搜索
   */
  onConfirm() {
    const keyword = this.data.keyword.trim();
    if (!keyword) {
      wx.showToast({
        title: '请输入搜索关键词',
        icon: 'none',
      });
      return;
    }
    this.doSearch(keyword);
  },

  /**
   * 执行搜索
   */
  async doSearch(keyword) {
    if (!keyword) {
      return;
    }

    this.setData({
      loading: true,
      hasSearched: true,
    });

    try {
      const results = await homeApi.search({
        keyword,
        page: 1,
        pageSize: 20,
      });

      // 保存搜索历史
      this.saveSearchHistory(keyword);

      this.setData({
        searchResults: results.list || [],
        loading: false,
      });
    } catch (error) {
      console.error('搜索失败:', error);
      this.setData({
        loading: false,
      });
      wx.showToast({
        title: '搜索失败，请重试',
        icon: 'none',
      });
    }
  },

  /**
   * 保存搜索历史
   */
  saveSearchHistory(keyword) {
    const storage = require('../../utils/storage');
    let history = storage.getSearchHistory();
    if (!Array.isArray(history)) {
      history = [];
    }

    // 移除重复项
    history = history.filter(item => item !== keyword);
    // 添加到开头
    history.unshift(keyword);
    // 最多保存10条
    history = history.slice(0, 10);

    storage.setSearchHistory(history);
    this.setData({
      searchHistory: history,
    });
  },

  /**
   * 点击搜索历史
   */
  onHistoryTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({
      keyword,
    });
    this.doSearch(keyword);
  },

  /**
   * 清除搜索历史
   */
  onClearHistory() {
    const storage = require('../../utils/storage');
    storage.setSearchHistory([]);
    this.setData({
      searchHistory: [],
    });
  },

  /**
   * 点击搜索结果项
   */
  onResultTap(e) {
    const item = e.currentTarget.dataset.item;
    // 跳转到详情页
    // 这里需要根据item的类型跳转到不同的详情页
    wx.showToast({
      title: '详情页开发中',
      icon: 'none',
    });
  },
});
