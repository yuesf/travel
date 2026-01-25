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
}

module.exports = {
  initNavBar
};
