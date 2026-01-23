/**
 * 骨架屏组件
 * 用于页面加载时显示占位内容，提升用户体验
 */

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 骨架屏类型：home-首页，list-列表，card-卡片，custom-自定义
    type: {
      type: String,
      value: 'list',
    },
    // 是否显示骨架屏
    loading: {
      type: Boolean,
      value: true,
    },
    // 行数（用于列表类型）
    rows: {
      type: Number,
      value: 3,
    },
    // 是否显示动画
    animated: {
      type: Boolean,
      value: true,
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
