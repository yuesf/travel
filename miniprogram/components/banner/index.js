/**
 * 轮播图组件
 * 支持图片和视频轮播，自动播放，手动滑动，点击事件
 */

const NavigationHelper = require('../../utils/navigation-helper.js')

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 轮播图数据数组
    banners: {
      type: Array,
      value: [],
    },
    // 是否自动播放
    autoplay: {
      type: Boolean,
      value: true,
    },
    // 自动播放间隔（毫秒）
    interval: {
      type: Number,
      value: 3000,
    },
    // 是否显示指示点
    indicatorDots: {
      type: Boolean,
      value: true,
    },
    // 指示点颜色
    indicatorColor: {
      type: String,
      value: 'rgba(0, 0, 0, 0.3)',
    },
    // 当前选中的指示点颜色
    indicatorActiveColor: {
      type: String,
      value: '#409EFF',
    },
    // 轮播图高度（rpx）
    height: {
      type: Number,
      value: 400,
    },
  },

  /**
   * 组件的初始数据
   */
  data: {
    currentIndex: 0, // 当前轮播索引
    shouldAutoplay: true, // 实际控制轮播是否自动播放（用于视频播放时暂停轮播）
  },

  /**
   * 组件生命周期函数--在组件实例进入页面节点树时执行
   */
  attached() {
    // 使用 properties 中的 autoplay 值初始化 shouldAutoplay
    this.setData({
      shouldAutoplay: this.properties.autoplay,
    });
    
    // 数据兼容性处理
    this.processBannersData();
  },

  /**
   * 组件生命周期函数--在 properties 改变时执行
   */
  observers: {
    'autoplay': function(autoplay) {
      // 当外部 autoplay 属性改变时，同步更新 shouldAutoplay
      // 但只有在视频未播放时（shouldAutoplay 为 true 时）才更新
      // 这样可以避免在视频播放期间被外部属性重置
      if (this.data.shouldAutoplay) {
        this.setData({
          shouldAutoplay: autoplay,
        });
      }
    },
  },

  /**
   * 组件的方法列表
   */
  methods: {
    /**
     * 处理轮播图数据兼容性
     */
    processBannersData() {
      const banners = this.properties.banners || [];
      const processedBanners = banners.map((banner) => {
        // 如果没有 linkType 但有 link，视为外部链接
        if (!banner.linkType && banner.link) {
          return {
            ...banner,
            linkType: 'EXTERNAL',
            linkValue: banner.link,
          };
        }
        // 如果都没有，视为无跳转
        if (!banner.linkType) {
          return {
            ...banner,
            linkType: 'NONE',
            linkValue: '',
          };
        }
        return banner;
      });
      
      // 更新数据
      if (JSON.stringify(banners) !== JSON.stringify(processedBanners)) {
        this.setData({
          banners: processedBanners,
        });
      }
    },

    /**
     * 轮播图切换事件
     */
    onSwiperChange(e) {
      const current = e.detail.current;
      this.setData({
        currentIndex: current,
      });
      this.triggerEvent('change', {
        current,
        banner: this.data.banners[current],
      });
    },

    /**
     * 点击轮播图
     */
    onBannerTap(e) {
      const index = e.currentTarget.dataset.index;
      const banner = this.data.banners[index];
      
      if (!banner) {
        return;
      }

      // 触发点击事件（保留，供父组件使用）
      this.triggerEvent('tap', {
        index,
        banner,
      });

      // 使用新的跳转逻辑
      try {
        NavigationHelper.handleBannerNavigation(banner);
      } catch (error) {
        console.error('轮播图跳转失败:', error);
        wx.showToast({
          title: '跳转失败',
          icon: 'none',
        });
      }
    },

    /**
     * 视频播放事件
     */
    onVideoPlay(e) {
      const index = e.currentTarget.dataset.index;
      const banner = this.data.banners[index];
      
      // 当视频开始播放时，暂停轮播
      this.setData({
        shouldAutoplay: false,
      });
      
      this.triggerEvent('videoPlay', {
        index,
        banner,
      });
    },

    /**
     * 视频结束事件
     */
    onVideoEnded(e) {
      const index = e.currentTarget.dataset.index;
      const banner = this.data.banners[index];
      
      // 视频结束时，恢复轮播自动播放（如果原本是开启的）
      if (this.properties.autoplay) {
        this.setData({
          shouldAutoplay: true,
        });
      }
      
      this.triggerEvent('videoEnded', {
        index,
        banner,
      });
    },

    /**
     * 图片加载失败事件
     */
    onImageError(e) {
      const index = e.currentTarget.dataset.index;
      const banner = this.data.banners[index];
      
      console.error('轮播图图片加载失败:', {
        index,
        image: banner?.image,
        banner,
      });
      
      // 触发图片错误事件（供父组件使用）
      this.triggerEvent('imageerror', {
        index,
        banner,
      });
    },
  },
});
