/**
 * 轮播图组件
 * 支持图片和视频轮播，自动播放，手动滑动，点击事件
 */

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

      // 触发点击事件
      this.triggerEvent('tap', {
        index,
        banner,
      });

      // 如果是图片，支持全屏预览
      if (banner.type === 'image' && banner.image) {
        const urls = this.data.banners
          .filter(item => item.type === 'image' && item.image)
          .map(item => item.image);
        const currentUrl = banner.image;
        
        wx.previewImage({
          urls,
          current: currentUrl,
        });
      }

      // 如果是视频，支持全屏播放
      if (banner.type === 'video' && banner.video) {
        // 视频播放由video组件自身处理
        // 这里可以触发自定义事件，由父组件处理
        this.triggerEvent('videoTap', {
          index,
          banner,
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
  },
});
