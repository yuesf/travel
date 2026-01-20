/**
 * 加载组件
 * 显示加载状态
 */

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 是否显示加载
    loading: {
      type: Boolean,
      value: false,
    },
    // 加载提示文字
    text: {
      type: String,
      value: '加载中...',
    },
    // 加载类型：spinner-旋转加载，dots-点状加载
    type: {
      type: String,
      value: 'spinner',
    },
    // 是否全屏显示
    fullscreen: {
      type: Boolean,
      value: false,
    },
  },

  /**
   * 组件的初始数据
   */
  data: {},

  /**
   * 组件的方法列表
   */
  methods: {},
});
