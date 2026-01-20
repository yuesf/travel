# 旅游平台后台管理系统

基于 Vue 3 + Element Plus 的后台管理系统前端项目。

## 技术栈

- Vue 3 (Composition API)
- Vue Router 4
- Pinia (状态管理)
- Element Plus (UI组件库)
- Axios (HTTP请求)
- Vite (构建工具)
- ECharts (图表库)

## 项目结构

```
admin-frontend/
├── src/
│   ├── api/           # API接口
│   ├── assets/        # 静态资源
│   ├── components/    # 公共组件
│   ├── layouts/       # 布局组件
│   ├── router/        # 路由配置
│   ├── store/         # 状态管理
│   ├── utils/         # 工具函数
│   ├── views/         # 页面组件
│   ├── App.vue        # 根组件
│   └── main.js        # 入口文件
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 环境要求

- Node.js >= 16.0.0
- npm >= 7.0.0

## 环境变量配置

### 高德地图API Key配置

项目使用高德地图进行位置选择功能，需要配置API Key：

1. **获取API Key**
   - 访问 [高德开放平台](https://lbs.amap.com/api/javascript-api/guide/abc/prepare)
   - 注册/登录账号
   - 创建应用并获取Web端(JS API)的Key

2. **配置API Key**
   ```bash
   # 复制示例文件
   cp .env.example .env.local
   
   # 编辑 .env.local 文件，填入您的API Key
   # VITE_AMAP_API_KEY=your_amap_api_key_here
   ```

3. **重启开发服务器**
   ```bash
   npm run dev
   ```

**注意**：
- `.env.local` 文件不会被提交到Git仓库（已在.gitignore中配置）
- 如果没有配置API Key，地图功能将显示提示信息，但仍可通过手动输入经纬度来设置位置
