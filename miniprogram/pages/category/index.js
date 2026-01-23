/**
 * 分类页
 */

const categoryApi = require('../../api/category');
const cartApi = require('../../api/cart');
const productApi = require('../../api/product');
const auth = require('../../utils/auth');
const constants = require('../../utils/constants');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 页面类型：固定为商品
    type: 'product',
    // 页面标题
    title: '商品分类',
    
    // 浏览模式标识（从 Icon 进入时隐藏购买功能）
    isBrowseMode: false,
    // 是否显示分类侧边栏
    showCategorySidebar: true,
    
    // 分类列表
    categories: [],
    // 当前选中的分类ID
    currentCategoryId: null,
    
    // 商品列表
    products: [],
    // 当前分类类型（DISPLAY、CONFIG、H5）
    currentCategoryType: null,
    // 分页信息
    page: 1,
    pageSize: constants.PAGINATION.DEFAULT_PAGE_SIZE,
    hasMore: true,
    loading: false,
    // 页面是否已加载（用于判断是否需要刷新）
    isPageLoaded: false,
    
    // 排序选项
    sortOptions: [
      { value: 'default', label: '默认排序' },
      { value: 'price_asc', label: '价格从低到高' },
      { value: 'price_desc', label: '价格从高到低' },
      { value: 'sales_desc', label: '销量最高' },
      { value: 'rating_desc', label: '评分最高' },
      { value: 'distance_asc', label: '距离最近' },
    ],
    // 当前排序
    currentSort: 'default',
    // 排序标签文本
    sortLabel: '默认排序',
    // 排序选择器显示状态
    showSortPicker: false,
    
    // 筛选条件
    filters: {
      minPrice: null, // 最低价格
      maxPrice: null, // 最高价格
      starLevel: null, // 星级（酒店）
      maxDistance: null, // 最大距离（km）
    },
    // 筛选面板显示状态
    showFilterPanel: false,
    // 是否有筛选条件
    hasFilters: false,
    
    // 是否显示规格选择弹窗
    showSpecModal: false,
    // 当前选择的商品详情（包含完整规格信息）
    currentProductDetail: null,
    // 选中的规格
    selectedSpec: {},
    // 数量
    quantity: 1,
    // 规格选择提示信息
    showSpecTip: false,
    specTipText: '',
    
    // 价格区间选项
    priceRanges: [
      { label: '不限', min: null, max: null },
      { label: '0-100', min: 0, max: 100 },
      { label: '100-300', min: 100, max: 300 },
      { label: '300-500', min: 300, max: 500 },
      { label: '500-1000', min: 500, max: 1000 },
      { label: '1000以上', min: 1000, max: null },
    ],
    // 星级选项（酒店）
    starLevels: [
      { label: '不限', value: null },
      { label: '五星', value: 5 },
      { label: '四星', value: 4 },
      { label: '三星', value: 3 },
      { label: '二星', value: 2 },
      { label: '一星', value: 1 },
    ],
    // 距离选项
    distanceRanges: [
      { label: '不限', value: null },
      { label: '1km内', value: 1 },
      { label: '3km内', value: 3 },
      { label: '5km内', value: 5 },
      { label: '10km内', value: 10 },
      { label: '20km内', value: 20 },
    ],
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('商品分类页加载', options);
    
    // 判断是否为浏览模式（从 Icon 进入）
    const isBrowseMode = options.fromIcon === 'true';
    const categoryId = options.categoryId ? parseInt(options.categoryId) : null;
    
    // 固定为商品类型
    this.setData({
      type: 'product',
      title: '商品分类',
      isBrowseMode: isBrowseMode,
      showCategorySidebar: !isBrowseMode, // 浏览模式时隐藏分类侧边栏
      currentCategoryId: categoryId,
    });
    
    // 设置导航栏标题
    if (isBrowseMode && categoryId) {
      // 浏览模式时，尝试获取分类名称作为标题
      wx.setNavigationBarTitle({
        title: '商品列表',
      });
    } else {
      wx.setNavigationBarTitle({
        title: '商品分类',
      });
    }
    
    if (isBrowseMode && categoryId) {
      // 浏览模式：直接加载指定分类的商品，不加载分类列表
      this.loadProducts(true).then(() => {
        this.setData({
          isPageLoaded: true,
        });
      });
    } else {
      // 正常模式：先加载分类列表，然后加载商品数据
      this.loadCategories().then(() => {
        this.loadProducts(true).then(() => {
          this.setData({
            isPageLoaded: true,
          });
        });
      });
    }
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
    
    // 如果页面已加载过，每次显示时都刷新数据
    if (this.data.isPageLoaded) {
      // 刷新分类列表和商品列表（保持当前选中的分类）
      this.loadCategories(true).then(() => {
        // 刷新商品列表
        this.loadProducts(true);
      });
    }
  },

  /**
   * 生命周期函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadProducts(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadProducts(false);
    }
  },


  /**
   * 加载分类列表
   * @param {boolean} keepCurrentSelection 是否保持当前选中的分类
   */
  async loadCategories(keepCurrentSelection = false) {
    try {
      const categories = await categoryApi.getCategories();
      
      // 处理分类数据
      const processedCategories = (categories || []).map(cat => ({
        id: cat.id,
        name: cat.name || '',
        icon: cat.icon || '',
      }));
      
      // 添加"全部"选项
      const allCategory = {
        id: null,
        name: '全部',
        icon: '',
      };
      
      const categoriesList = [allCategory, ...processedCategories];
      
      // 确定要选中的分类ID
      let selectedCategoryId;
      if (keepCurrentSelection && this.data.currentCategoryId !== null) {
        // 保持当前选中的分类，但需要验证该分类是否还存在
        const exists = categoriesList.some(cat => cat.id === this.data.currentCategoryId);
        selectedCategoryId = exists ? this.data.currentCategoryId : (categoriesList.length > 0 ? categoriesList[0].id : null);
      } else {
        // 默认选中第一个分类
        selectedCategoryId = categoriesList.length > 0 ? categoriesList[0].id : null;
      }
      
      this.setData({
        categories: categoriesList,
        currentCategoryId: selectedCategoryId,
      });
    } catch (error) {
      console.error('加载分类列表失败:', error);
      wx.showToast({
        title: '加载分类失败',
        icon: 'none',
      });
    }
  },

  /**
   * 加载商品列表
   * @param {boolean} refresh 是否刷新（重置分页）
   */
  async loadProducts(refresh = false) {
    // 如果正在加载，直接返回
    if (this.data.loading) {
      console.log('正在加载中，跳过本次请求');
      return Promise.resolve();
    }

    const page = refresh ? 1 : this.data.page;
    
    this.setData({
      loading: true,
    });

    try {
      // 构建查询参数
      const params = {
        page,
        pageSize: this.data.pageSize,
      };
      
      // 添加分类筛选
      if (this.data.currentCategoryId !== null && this.data.currentCategoryId !== undefined) {
        params.categoryId = this.data.currentCategoryId;
        console.log('查询分类商品，分类ID:', this.data.currentCategoryId);
      } else {
        console.log('查询全部商品（未选择分类）');
      }
      
      // 添加排序
      if (this.data.currentSort !== 'default') {
        params.sort = this.data.currentSort;
      }
      
      // 添加价格筛选
      if (this.data.filters.minPrice !== null && this.data.filters.minPrice !== undefined) {
        params.minPrice = this.data.filters.minPrice;
      }
      if (this.data.filters.maxPrice !== null && this.data.filters.maxPrice !== undefined) {
        params.maxPrice = this.data.filters.maxPrice;
      }
      
      console.log('商品列表查询参数:', params);
      
      // 调用商品列表API
      const response = await categoryApi.getProducts(params);
      
      console.log('商品列表API响应:', response);
      
      // 处理数据
      const items = (response.list || response.data || []).map(item => ({
        id: item.id,
        name: item.name || '',
        image: item.image || item.coverImage || '',
        coverImage: item.image || item.coverImage || '',
        price: item.price || item.minPrice || 0,
        minPrice: item.price || item.minPrice || 0,
        originalPrice: item.originalPrice || 0,
        rating: item.rating || 0,
        reviewCount: item.reviewCount || 0,
        sales: item.sales || 0,
        location: item.location || item.address || item.city || '',
        address: item.address || item.location || item.city || '',
        city: item.city || '',
        starLevel: item.starLevel || 0,
        productType: 'PRODUCT',
        categoryType: item.categoryType || null, // 保存分类类型
        h5Link: item.h5Link || null, // 保存H5链接
        description: item.description || '', // 保存商品描述
      }));
      
      // 判断当前分类类型（从第一个商品的categoryType判断，仅在刷新时更新）
      if (refresh && items.length > 0 && items[0].categoryType) {
        this.setData({
          currentCategoryType: items[0].categoryType,
        });
      }
      
      // 更新列表数据
      const products = refresh ? items : [...this.data.products, ...items];
      const hasMore = items.length >= this.data.pageSize;
      
      console.log('商品列表加载成功，数量:', items.length, '总数量:', products.length, '是否有更多:', hasMore);
      
      this.setData({
        products,
        page: page + 1,
        hasMore,
        loading: false,
      });
    } catch (error) {
      console.error('加载商品列表失败:', error);
      this.setData({
        loading: false,
      });
      wx.showToast({
        title: error.message || '加载失败，请重试',
        icon: 'none',
        duration: 2000,
      });
    }
  },


  /**
   * 选择分类
   */
  onCategoryTap(e) {
    const { categoryid } = e.currentTarget.dataset;
    console.log('点击分类，categoryid:', categoryid);
    
    // 处理分类ID：null 或 'null' 字符串都转换为 null
    let categoryId = null;
    if (categoryid !== null && categoryid !== undefined && categoryid !== 'null') {
      categoryId = parseInt(categoryid);
      // 如果转换失败，使用 null
      if (isNaN(categoryId)) {
        categoryId = null;
      }
    }
    
    console.log('处理后的分类ID:', categoryId, '当前分类ID:', this.data.currentCategoryId);
    
    // 如果点击的是当前已选中的分类，不重复加载
    if (categoryId === this.data.currentCategoryId) {
      console.log('分类未变化，跳过加载');
      return;
    }
    
    // 更新选中的分类ID
    this.setData({
      currentCategoryId: categoryId,
      products: [], // 清空当前商品列表，准备加载新分类的商品
    });
    
    console.log('开始加载分类商品列表，分类ID:', categoryId);
    
    // 刷新商品列表
    this.loadProducts(true);
  },

  /**
   * 显示排序选择器
   */
  onShowSortPicker() {
    this.setData({
      showSortPicker: true,
    });
  },

  /**
   * 隐藏排序选择器
   */
  onHideSortPicker() {
    this.setData({
      showSortPicker: false,
    });
  },

  /**
   * 选择排序
   */
  onSortSelect(e) {
    const { sort } = e.currentTarget.dataset;
    
    if (sort === this.data.currentSort) {
      this.onHideSortPicker();
      return;
    }
    
    const option = this.data.sortOptions.find(opt => opt.value === sort);
    const sortLabel = option ? option.label : '排序';
    
    this.setData({
      currentSort: sort,
      sortLabel,
      showSortPicker: false,
    });
    
    // 刷新列表
    this.loadProducts(true);
  },

  /**
   * 显示筛选面板
   */
  onShowFilterPanel() {
    this.setData({
      showFilterPanel: true,
    });
  },

  /**
   * 隐藏筛选面板
   */
  onHideFilterPanel() {
    this.setData({
      showFilterPanel: false,
    });
  },

  /**
   * 选择价格区间
   */
  onPriceRangeSelect(e) {
    const { index } = e.currentTarget.dataset;
    const range = this.data.priceRanges[index];
    
    this.setData({
      'filters.minPrice': range.min,
      'filters.maxPrice': range.max,
    });
    
    this.updateHasFilters();
  },

  /**
   * 选择星级
   */
  onStarLevelSelect(e) {
    const { value } = e.currentTarget.dataset;
    const starLevel = value === 'null' || value === null ? null : parseInt(value);
    
    this.setData({
      'filters.starLevel': starLevel,
    });
    
    this.updateHasFilters();
  },

  /**
   * 选择距离
   */
  onDistanceSelect(e) {
    const { value } = e.currentTarget.dataset;
    const distance = value === 'null' || value === null ? null : parseInt(value);
    
    this.setData({
      'filters.maxDistance': distance,
    });
    
    this.updateHasFilters();
  },

  /**
   * 重置筛选条件
   */
  onResetFilters() {
    this.setData({
      filters: {
        minPrice: null,
        maxPrice: null,
        starLevel: null,
        maxDistance: null,
      },
      hasFilters: false,
    });
  },

  /**
   * 确认筛选
   */
  onConfirmFilters() {
    this.onHideFilterPanel();
    // 刷新列表
    this.loadProducts(true);
  },

  /**
   * 商品卡片点击事件
   */
  onProductTap(e) {
    const { product } = e.currentTarget.dataset;
    if (!product || !product.id) {
      return;
    }
    
    // 跳转到商品详情页
    const url = `/pages/detail/index?type=product&id=${product.id}`;
    wx.navigateTo({
      url,
    });
  },

  /**
   * H5类型商品预约按钮点击事件
   */
  onReserveTap(e) {
    const { h5Link } = e.currentTarget.dataset;
    if (!h5Link) {
      wx.showToast({
        title: 'H5链接不存在',
        icon: 'none',
      });
      return;
    }
    
    // 验证 URL 格式
    if (!h5Link.startsWith('http://') && !h5Link.startsWith('https://')) {
      wx.showToast({
        title: '链接地址无效',
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

  /**
   * 添加到购物车
   */
  async onAddToCart(e) {
    // 阻止事件冒泡（如果事件对象有该方法）
    if (e && typeof e.stopPropagation === 'function') {
      e.stopPropagation();
    }
    
    const { showSpecModal, currentProductDetail, selectedSpec } = this.data;
    
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
    
    const { product } = e.currentTarget.dataset;
    if (!product || !product.id) {
      return;
    }
    
    // 检查商品是否有规格
    let hasSpecifications = false;
    let detail = product;
    
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
        currentProductDetail: detail,
        selectedSpec: defaultSelectedSpec,
        quantity: 1,
      });
      return;
    }
    
    // 没有规格，直接添加到购物车
    await this.doAddToCart(product);
  },

  /**
   * 执行加入购物车操作
   */
  async doAddToCart(product) {
    const { quantity = 1, selectedSpec, currentProductDetail } = this.data;
    const targetProduct = product || (currentProductDetail ? { id: currentProductDetail.id } : null);
    
    if (!targetProduct || !targetProduct.id) {
      return;
    }
    
    try {
      // 构建购物车数据（商品类型）
      const itemType = 'PRODUCT';
      
      const cartData = {
        itemType,
        itemId: targetProduct.id,
        quantity: quantity || 1,
      };
      
      // 调用API添加到购物车（云购物车方案：立即保存到后端数据库）
      await cartApi.addToCart(cartData);
      
      // 确保loading已关闭，延迟显示成功提示
      setTimeout(() => {
        wx.showToast({
          title: '添加购物车成功',
          icon: 'success',
          duration: 2000,
        });
      }, 100);
      
      // 更新全局购物车数量（从后端实时查询）
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
    } catch (error) {
      // 如果是未登录错误，不显示错误提示（已经跳转到登录页了）
      if (error.isAuthError) {
        return;
      }
      
      console.error('添加到购物车失败:', error);
      wx.showToast({
        title: error.message || '添加失败，请重试',
        icon: 'none',
        duration: 2000,
      });
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
    const { quantity, currentProductDetail } = this.data;
    
    // 验证库存
    if (currentProductDetail && currentProductDetail.stock !== undefined && quantity >= currentProductDetail.stock) {
      wx.showToast({
        title: `库存不足，仅剩${currentProductDetail.stock}件`,
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

  /**
   * 更新是否有筛选条件
   */
  updateHasFilters() {
    const { filters } = this.data;
    const hasFilters = filters.minPrice !== null || 
                       filters.maxPrice !== null || 
                       filters.starLevel !== null || 
                       filters.maxDistance !== null;
    this.setData({
      hasFilters,
    });
  },
});
