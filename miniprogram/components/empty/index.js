/**
 * 空状态组件
 * 显示空状态提示
 */

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    // 提示文字
    text: {
      type: String,
      value: '暂无数据',
    },
    // 空状态图片
    image: {
      type: String,
      value: '',
    },
    // 是否显示操作按钮
    showAction: {
      type: Boolean,
      value: false,
    },
    // 操作按钮文字
    actionText: {
      type: String,
      value: '去逛逛',
    },
  },

  /**
   * 组件的初始数据
   */
  data: {},

  /**
   * 组件的方法列表
   */
  methods: {
    /**
     * 点击操作按钮
     */
    onActionTap() {
      this.triggerEvent('action', {});
    },
  },
});
