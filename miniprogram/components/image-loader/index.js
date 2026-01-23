/**
 * 图片加载组件
 * 支持占位图、加载状态、错误处理
 */

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 图片地址
    src: {
      type: String,
      value: '',
    },
    // 图片模式
    mode: {
      type: String,
      value: 'aspectFill',
    },
    // 占位图地址
    placeholder: {
      type: String,
      value: '/static/images/default-product.png',
    },
    // 是否懒加载
    lazyLoad: {
      type: Boolean,
      value: true,
    },
    // 是否显示加载动画
    showLoading: {
      type: Boolean,
      value: true,
    },
    // 图片类名
    className: {
      type: String,
      value: '',
    },
  },

  /**
   * 组件的初始数据
   */
  data: {
    imageLoaded: false,
    imageError: false,
    loading: true,
  },

  /**
   * 组件的方法列表
   */
  methods: {
    /**
     * 图片加载成功
     */
    onImageLoad(e) {
      this.setData({
        imageLoaded: true,
        loading: false,
        imageError: false,
      });
      this.triggerEvent('load', e.detail);
    },

    /**
     * 图片加载失败
     */
    onImageError(e) {
      this.setData({
        imageError: true,
        loading: false,
        imageLoaded: false,
      });
      this.triggerEvent('error', e.detail);
    },

    /**
     * 图片点击事件
     */
    onImageTap(e) {
      this.triggerEvent('tap', e.detail);
    },
  },
});
