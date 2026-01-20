import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from './guards'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: {
      requiresAuth: true,
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '首页',
          requiresAuth: true,
        },
      },
      {
        path: 'attractions',
        name: 'Attractions',
        component: () => import('@/views/attractions/AttractionList.vue'),
        meta: {
          title: '景点管理',
          requiresAuth: true,
        },
      },
      {
        path: 'attractions/create',
        name: 'AttractionCreate',
        component: () => import('@/views/attractions/AttractionForm.vue'),
        meta: {
          title: '创建景点',
          requiresAuth: true,
        },
      },
      {
        path: 'attractions/edit/:id',
        name: 'AttractionEdit',
        component: () => import('@/views/attractions/AttractionForm.vue'),
        meta: {
          title: '编辑景点',
          requiresAuth: true,
        },
      },
      // 酒店管理
      {
        path: 'hotels',
        name: 'Hotels',
        component: () => import('@/views/hotels/HotelList.vue'),
        meta: {
          title: '酒店管理',
          requiresAuth: true,
        },
      },
      {
        path: 'hotels/create',
        name: 'HotelCreate',
        component: () => import('@/views/hotels/HotelForm.vue'),
        meta: {
          title: '创建酒店',
          requiresAuth: true,
        },
      },
      {
        path: 'hotels/edit/:id',
        name: 'HotelEdit',
        component: () => import('@/views/hotels/HotelForm.vue'),
        meta: {
          title: '编辑酒店',
          requiresAuth: true,
        },
      },
      // 商品管理
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/products/ProductList.vue'),
        meta: {
          title: '商品管理',
          requiresAuth: true,
        },
      },
      {
        path: 'products/create',
        name: 'ProductCreate',
        component: () => import('@/views/products/ProductForm.vue'),
        meta: {
          title: '创建商品',
          requiresAuth: true,
        },
      },
      {
        path: 'products/edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/products/ProductForm.vue'),
        meta: {
          title: '编辑商品',
          requiresAuth: true,
        },
      },
      // 分类管理
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/categories/CategoryList.vue'),
        meta: {
          title: '分类管理',
          requiresAuth: true,
        },
      },
      // 优惠券管理 - 待实现
      // {
      //   path: 'coupons',
      //   name: 'Coupons',
      //   component: () => import('@/views/coupons/CouponList.vue'),
      //   meta: {
      //     title: '优惠券管理',
      //     requiresAuth: true,
      //   },
      // },
      // {
      //   path: 'coupons/create',
      //   name: 'CouponCreate',
      //   component: () => import('@/views/coupons/CouponForm.vue'),
      //   meta: {
      //     title: '创建优惠券',
      //     requiresAuth: true,
      //   },
      // },
      // {
      //   path: 'coupons/edit/:id',
      //   name: 'CouponEdit',
      //   component: () => import('@/views/coupons/CouponForm.vue'),
      //   meta: {
      //     title: '编辑优惠券',
      //     requiresAuth: true,
      //   },
      // },
      // 订单管理 - 待实现
      // {
      //   path: 'orders',
      //   name: 'Orders',
      //   component: () => import('@/views/orders/OrderList.vue'),
      //   meta: {
      //     title: '订单管理',
      //     requiresAuth: true,
      //   },
      // },
      // {
      //   path: 'orders/:id',
      //   name: 'OrderDetail',
      //   component: () => import('@/views/orders/OrderDetail.vue'),
      //   meta: {
      //     title: '订单详情',
      //     requiresAuth: true,
      //   },
      // },
      // 用户管理 - 待实现
      // {
      //   path: 'users',
      //   name: 'Users',
      //   component: () => import('@/views/users/UserList.vue'),
      //   meta: {
      //     title: '用户管理',
      //     requiresAuth: true,
      //   },
      // },
      // {
      //   path: 'users/:id',
      //   name: 'UserDetail',
      //   component: () => import('@/views/users/UserDetail.vue'),
      //   meta: {
      //     title: '用户详情',
      //     requiresAuth: true,
      //   },
      // },
      {
        path: 'miniprogram',
        component: () => import('@/views/miniprogram/MiniProgramLayout.vue'),
        redirect: '/miniprogram/home',
        meta: {
          title: '小程序管理',
          requiresAuth: true,
        },
        children: [
          {
            path: 'home',
            name: 'MiniProgramHome',
            component: () => import('@/views/miniprogram/components/HomeConfig.vue'),
            meta: {
              title: '首页配置',
              requiresAuth: true,
            },
          },
          {
            path: 'ads',
            name: 'MiniProgramAds',
            component: () => import('@/views/miniprogram/components/AdConfig.vue'),
            meta: {
              title: '广告位配置',
              requiresAuth: true,
            },
          },
          {
            path: 'logo',
            name: 'MiniProgramLogo',
            component: () => import('@/views/miniprogram/components/LogoConfig.vue'),
            meta: {
              title: 'Logo配置',
              requiresAuth: true,
            },
          },
          {
            path: 'statistics',
            name: 'MiniProgramStatistics',
            component: () => import('@/views/miniprogram/components/Statistics.vue'),
            meta: {
              title: '数据统计',
              requiresAuth: true,
            },
          },
        ],
      },
      // 文章管理
      {
        path: 'articles',
        name: 'Articles',
        component: () => import('@/views/articles/ArticleList.vue'),
        meta: {
          title: '文章管理',
          requiresAuth: true,
        },
      },
      {
        path: 'articles/create',
        name: 'ArticleCreate',
        component: () => import('@/views/articles/ArticleForm.vue'),
        meta: {
          title: '创建文章',
          requiresAuth: true,
        },
      },
      {
        path: 'articles/edit/:id',
        name: 'ArticleEdit',
        component: () => import('@/views/articles/ArticleForm.vue'),
        meta: {
          title: '编辑文章',
          requiresAuth: true,
        },
      },
      {
        path: 'articles/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/articles/ArticleDetail.vue'),
        meta: {
          title: '文章详情',
          requiresAuth: true,
        },
      },
      {
        path: 'articles/categories',
        name: 'ArticleCategories',
        component: () => import('@/views/articles/CategoryList.vue'),
        meta: {
          title: '文章分类管理',
          requiresAuth: true,
        },
      },
      {
        path: 'articles/tags',
        name: 'ArticleTags',
        component: () => import('@/views/articles/TagList.vue'),
        meta: {
          title: '文章标签管理',
          requiresAuth: true,
        },
      },
      // 系统管理
      {
        path: 'system/payment-config',
        name: 'PaymentConfig',
        component: () => import('@/views/system/PaymentConfig.vue'),
        meta: {
          title: '支付配置',
          requiresAuth: true,
        },
      },
      {
        path: 'system/merchant-config',
        name: 'MerchantConfig',
        component: () => import('@/views/system/MerchantConfig.vue'),
        meta: {
          title: '商家配置',
          requiresAuth: true,
        },
      },
      // 系统管理 - 待实现
      // {
      //   path: 'system/admins',
      //   name: 'AdminManager',
      //   component: () => import('@/views/system/AdminManager.vue'),
      //   meta: {
      //     title: '管理员管理',
      //     requiresAuth: true,
      //   },
      // },
      // {
      //   path: 'system/roles',
      //   name: 'RoleManager',
      //   component: () => import('@/views/system/RoleManager.vue'),
      //   meta: {
      //     title: '角色权限',
      //     requiresAuth: true,
      //   },
      // },
      // {
      //   path: 'system/logs',
      //   name: 'OperationLog',
      //   component: () => import('@/views/system/OperationLog.vue'),
      //   meta: {
      //     title: '操作日志',
      //     requiresAuth: true,
      //   },
      // },
      // 403页面
      {
        path: '403',
        name: 'Forbidden',
        component: () => import('@/views/error/403.vue'),
        meta: {
          title: '无权限访问',
          requiresAuth: false,
        },
      },
    ],
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false,
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 设置路由守卫
setupRouterGuards(router)

export default router
