/**
 * 导航跳转助手
 * 统一处理轮播图点击跳转逻辑
 */

/**
 * 处理轮播图点击跳转
 * @param {Object} banner - 轮播图配置对象
 * @param {String} banner.linkType - 链接类型
 * @param {String|Number} banner.linkValue - 链接值
 * @param {String} banner.type - 轮播图类型（image/video）
 * @param {String} banner.image - 图片URL
 * @param {String} banner.video - 视频URL
 * @param {String} banner.link - 旧版本链接（兼容）
 */
function handleBannerNavigation(banner) {
  if (!banner) {
    console.error('Banner 配置为空')
    return
  }

  // 兼容性处理：如果没有 linkType 但有 link，视为外部链接
  let linkType = banner.linkType
  let linkValue = banner.linkValue

  if (!linkType && banner.link) {
    linkType = 'EXTERNAL'
    linkValue = banner.link
  } else if (!linkType) {
    linkType = 'NONE'
  }

  // 根据链接类型进行跳转
  switch (linkType) {
    case 'NONE':
      // 无跳转，预览图片或视频
      previewBanner(banner)
      break

    case 'EXTERNAL':
      // 外部链接，跳转到 webview 页面
      navigateToExternal(linkValue, banner.title)
      break

    case 'PRODUCT_CATEGORY':
      // 商品分类，跳转到商品列表页
      navigateToProductList(linkValue)
      break

    case 'PRODUCT':
      // 商品详情，跳转到商品详情页
      navigateToProductDetail(linkValue)
      break

    case 'ARTICLE_CATEGORY':
      // 文章分类，跳转到文章列表页
      navigateToArticleList(linkValue)
      break

    case 'ARTICLE':
      // 文章详情，跳转到文章详情页
      navigateToArticleDetail(linkValue)
      break

    case 'ATTRACTION':
      // 景点，跳转到景点详情页
      navigateToAttractionDetail(linkValue)
      break

    case 'HOTEL':
      // 酒店，跳转到酒店详情页
      navigateToHotelDetail(linkValue)
      break

    case 'MAP':
      // 地图，跳转到地图页面
      navigateToMap(linkValue)
      break

    default:
      console.warn('未知的链接类型:', linkType)
      wx.showToast({
        title: '跳转类型不支持',
        icon: 'none',
      })
  }
}

/**
 * 预览轮播图（无跳转时）
 */
function previewBanner(banner) {
  if (banner.type === 'image' && banner.image) {
    // 图片预览
    wx.previewImage({
      urls: [banner.image],
      current: banner.image,
    })
  } else if (banner.type === 'video') {
    // 视频由 video 组件自身处理，这里提示用户
    wx.showToast({
      title: '请直接点击视频播放',
      icon: 'none',
    })
  }
}

/**
 * 预览多张图片（用于轮播图组件）
 * @param {Array} banners - 所有轮播图
 * @param {Number} currentIndex - 当前索引
 */
function previewBannerImage(banners, currentIndex) {
  if (!banners || banners.length === 0) {
    return
  }

  // 获取所有图片URL
  const urls = banners
    .filter((item) => item.type === 'image' && item.image)
    .map((item) => item.image)

  if (urls.length === 0) {
    wx.showToast({
      title: '没有可预览的图片',
      icon: 'none',
    })
    return
  }

  const currentBanner = banners[currentIndex]
  const currentUrl = currentBanner && currentBanner.image ? currentBanner.image : urls[0]

  wx.previewImage({
    urls,
    current: currentUrl,
  })
}

/**
 * 跳转到外部链接
 */
function navigateToExternal(url, title) {
  if (!url) {
    wx.showToast({
      title: '链接地址为空',
      icon: 'none',
    })
    return
  }

  // 检查 URL 格式
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    wx.showToast({
      title: '链接格式不正确',
      icon: 'none',
    })
    return
  }

  wx.navigateTo({
    url: `/pages/webview/index?url=${encodeURIComponent(url)}&title=${encodeURIComponent(title || '加载中...')}`,
    fail: (err) => {
      console.error('跳转到 webview 失败:', err)
      wx.showToast({
        title: '跳转失败',
        icon: 'none',
      })
    },
  })
}

/**
 * 跳转到商品列表页（按分类筛选）
 */
function navigateToProductList(categoryId) {
  if (!categoryId) {
    wx.showToast({
      title: '分类ID为空',
      icon: 'none',
    })
    return
  }

  wx.navigateTo({
    url: `/pages/product/list?categoryId=${categoryId}`,
    fail: (err) => {
      console.error('跳转到商品列表失败:', err)
      wx.showToast({
        title: '跳转失败',
        icon: 'none',
      })
    },
  })
}

/**
 * 跳转到商品详情页
 */
function navigateToProductDetail(productId) {
  if (!productId) {
    wx.showToast({
      title: '商品ID为空',
      icon: 'none',
    })
    return
  }

  wx.navigateTo({
    url: `/pages/detail/index?id=${productId}&type=product`,
    fail: (err) => {
      console.error('跳转到商品详情失败:', err)
      // 可能是商品已删除或下架
      wx.showModal({
        title: '提示',
        content: '该商品已下架或不存在',
        showCancel: false,
      })
    },
  })
}

/**
 * 跳转到文章列表页（按分类筛选）
 * @param {Number} categoryId - 分类ID
 * @param {String} pageTitle - 页面标题（可选，如果不传则尝试从当前页面获取）
 */
function navigateToArticleList(categoryId, pageTitle) {
  if (!categoryId) {
    wx.showToast({
      title: '分类ID为空',
      icon: 'none',
    })
    return
  }

  // 如果没有传递页面标题，尝试从当前页面获取
  if (!pageTitle) {
    try {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      
      if (currentPage) {
        // 尝试从页面数据中获取标题
        if (currentPage.data && currentPage.data.title) {
          pageTitle = currentPage.data.title
        } else if (currentPage.options && currentPage.options.title) {
          pageTitle = currentPage.options.title
        } else if (currentPage.route) {
          // 从路由名称推断标题（例如：pages/home/index -> 首页）
          const routeName = currentPage.route.split('/').pop()
          const routeTitleMap = {
            'index': '首页',
            'home': '首页',
            'category': '分类',
            'search': '搜索',
          }
          pageTitle = routeTitleMap[routeName] || '文章'
        }
      }
    } catch (e) {
      console.warn('获取页面标题失败:', e)
    }
    
    // 如果仍然没有获取到标题，使用默认值
    if (!pageTitle) {
      pageTitle = '文章'
    }
  }

  wx.navigateTo({
    url: `/pages/article/list?categoryId=${categoryId}&pageTitle=${encodeURIComponent(pageTitle)}`,
    fail: (err) => {
      console.error('跳转到文章列表失败:', err)
      wx.showToast({
        title: '跳转失败',
        icon: 'none',
      })
    },
  })
}

/**
 * 跳转到文章详情页
 */
function navigateToArticleDetail(articleId) {
  if (!articleId) {
    wx.showToast({
      title: '文章ID为空',
      icon: 'none',
    })
    return
  }

  wx.navigateTo({
    url: `/pages/article/detail?id=${articleId}`,
    fail: (err) => {
      console.error('跳转到文章详情失败:', err)
      // 可能是文章已删除或下架
      wx.showModal({
        title: '提示',
        content: '该文章已下架或不存在',
        showCancel: false,
      })
    },
  })
}

/**
 * 跳转到景点详情页
 */
function navigateToAttractionDetail(attractionId) {
  if (!attractionId) {
    wx.showToast({
      title: '景点ID为空',
      icon: 'none',
    })
    return
  }

  wx.navigateTo({
    url: `/pages/attraction/detail?id=${attractionId}`,
    fail: (err) => {
      console.error('跳转到景点详情失败:', err)
      wx.showModal({
        title: '提示',
        content: '该景点已下架或不存在',
        showCancel: false,
      })
    },
  })
}

/**
 * 跳转到酒店详情页
 */
function navigateToHotelDetail(hotelId) {
  if (!hotelId) {
    wx.showToast({
      title: '酒店ID为空',
      icon: 'none',
    })
    return
  }

  wx.navigateTo({
    url: `/pages/hotel/detail?id=${hotelId}`,
    fail: (err) => {
      console.error('跳转到酒店详情失败:', err)
      wx.showModal({
        title: '提示',
        content: '该酒店已下架或不存在',
        showCancel: false,
      })
    },
  })
}

/**
 * 跳转到地图页面
 */
function navigateToMap(mapId) {
  if (mapId) {
    // 跳转到具体地图
    wx.navigateTo({
      url: `/pages/map/list?id=${mapId}`,
      fail: (err) => {
        console.error('跳转到地图失败:', err)
        wx.showToast({
          title: '跳转失败',
          icon: 'none',
        })
      },
    })
  } else {
    // 跳转到地图列表
    wx.navigateTo({
      url: `/pages/map/list`,
      fail: (err) => {
        console.error('跳转到地图列表失败:', err)
        wx.showToast({
          title: '跳转失败',
          icon: 'none',
        })
      },
    })
  }
}

module.exports = {
  handleBannerNavigation,
  previewBannerImage,
}
