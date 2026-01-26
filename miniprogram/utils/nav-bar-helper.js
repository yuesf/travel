/**
 * 导航栏辅助工具
 * 用于在页面中快速集成自定义导航栏
 */

/**
 * 初始化导航栏
 * 在页面的 onReady 生命周期中调用
 * @param {Object} page - 页面实例（this）
 * @param {string} navBarId - 导航栏组件ID，默认为 'navBar'
 */
function initNavBar(page, navBarId = 'navBar') {
  try {
    // 延迟执行，确保页面完全初始化
    setTimeout(() => {
      try {
        const navBar = page.selectComponent(`#${navBarId}`);
        if (navBar) {
          const navBarHeight = navBar.getNavBarHeight();
          page.setData({
            navBarTotalHeight: navBarHeight
          });
          console.log('导航栏高度已设置:', navBarHeight);
        } else {
          console.warn('未找到导航栏组件');
        }
      } catch (error) {
        // 忽略框架内部错误
        if (error && error.message && 
            (error.message.includes('__subPageFrameEndTime__') || 
             error.message.includes('Cannot read property'))) {
          console.warn('导航栏初始化时遇到框架内部错误（已忽略）:', error.message);
        } else {
          console.error('初始化导航栏失败:', error);
        }
      }
    }, 50); // 延迟50ms，确保页面完全初始化
  } catch (error) {
    console.error('初始化导航栏失败:', error);
  }
}

module.exports = {
  initNavBar
};
