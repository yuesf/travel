# 旅游平台小程序

微信小程序前端项目，基于微信小程序原生框架开发。

## 项目结构

```
miniprogram/
├── app.js                 # 小程序入口文件
├── app.json              # 小程序全局配置
├── app.wxss              # 小程序全局样式
├── sitemap.json          # 小程序搜索配置
├── project.config.json   # 项目配置文件
│
├── pages/                # 页面目录
│   ├── home/            # 首页
│   ├── category/        # 分类页
│   ├── cart/            # 购物车页
│   └── mine/            # 我的页面
│       └── login.js      # 登录页
│
├── utils/                # 工具函数目录
│   ├── request.js       # 网络请求封装
│   ├── auth.js          # 认证相关工具
│   ├── storage.js       # 存储工具
│   └── constants.js     # 常量定义
│
└── styles/              # 样式目录
    ├── common.wxss      # 公共样式
    └── variables.wxss   # 样式变量
```

## 开发说明

### 环境要求

- 微信开发者工具最新版本
- 小程序基础库版本 2.10.0+

### 配置说明

1. **project.config.json**
   - 修改 `appid` 为你的小程序AppID
   - 修改 `projectname` 为你的项目名称

2. **utils/constants.js**
   - 修改 `API_BASE_URL` 为你的后端API地址
   - 根据实际情况调整其他常量配置

### 功能说明

#### 网络请求

使用 `utils/request.js` 封装的请求方法：

```javascript
const request = require('../../utils/request');

// GET请求
request.get('/miniprogram/home', { page: 1 }).then(data => {
  console.log(data);
});

// POST请求
request.post('/miniprogram/auth/login', { phone: '13800138000', code: '123456' }).then(data => {
  console.log(data);
});
```

#### 认证管理

使用 `utils/auth.js` 管理用户认证：

```javascript
const auth = require('../../utils/auth');

// 检查是否已登录
if (auth.isLoggedIn()) {
  const userInfo = auth.getUserInfo();
  console.log(userInfo);
}

// 设置Token
auth.setToken('your-token');

// 退出登录
auth.logout();
```

#### 存储管理

使用 `utils/storage.js` 管理本地存储：

```javascript
const storage = require('../../utils/storage');

// 存储数据
storage.setStorage('key', 'value', true); // true表示同步存储

// 获取数据
const value = storage.getStorage('key', 'default', true);

// 便捷方法
storage.setToken('token');
storage.getToken();
storage.setUserInfo({ name: '张三' });
storage.getUserInfo();
```

## 开发规范

- 使用2个空格缩进
- 使用单引号
- 语句末尾加分号
- 关键逻辑添加注释
- 遵循小程序开发规范

## 常见问题

### 小程序无法访问 localhost 的问题

如果遇到视频或图片加载失败的错误（如 `Failed to load media http://localhost:8080/...`），这是因为小程序无法访问 `localhost`。

**解决方案：**

1. **方案1：使用本机IP地址（推荐）**
   - 在 `app.js` 的 `onLaunch` 方法中添加：
   ```javascript
   onLaunch() {
     // ... 其他代码
     
     // 设置媒体资源基础URL为本机IP地址
     // 将 192.168.1.100 替换为你的本机IP地址
     this.globalData.mediaBaseUrl = 'http://192.168.1.100:8080';
   }
   ```
   - 获取本机IP地址：
     - Windows: 在命令行运行 `ipconfig`，查找 IPv4 地址
     - Mac/Linux: 在终端运行 `ifconfig` 或 `ip addr`，查找局域网IP

2. **方案2：配置微信开发者工具**
   - 在微信开发者工具中，点击右上角"详情"
   - 在"本地设置"中勾选"不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"
   - 注意：此方法可能对 `<video>` 和 `<image>` 组件无效

3. **方案3：使用代理或内网穿透**
   - 使用 ngrok、natapp 等工具将本地服务映射到公网域名
   - 修改后端配置中的 `travel.file.access-url` 为映射后的域名

## 注意事项

1. 首次使用需要在微信开发者工具中配置AppID
2. 需要确保后端API服务已启动（默认端口8080）
3. TabBar图标需要自行准备，放置在 `assets/icons/` 目录下
4. 生产环境需要修改 `API_BASE_URL` 为实际服务器地址
5. 开发环境中如果遇到 localhost 无法访问，请参考上面的"常见问题"部分