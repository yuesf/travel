import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 根据环境设置 base 路径
  // 生产环境部署在 /admin/ 路径下（nginx 配置），开发环境在根路径
  const base = mode === 'production' ? '/admin/' : '/'
  
  return {
    base,
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        resolvers: [ElementPlusResolver()],
      }),
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: resolve(__dirname, 'dist'),
      emptyOutDir: true,
      assetsDir: 'assets',
      // 确保资源路径使用 base 路径
      assetsInlineLimit: 4096,
      rollupOptions: {
        output: {
          manualChunks(id) {
            // 将 node_modules 中的依赖打包到单独的 chunk
            if (id.includes('node_modules')) {
              if (id.includes('element-plus')) {
                return 'element-plus'
              }
              if (id.includes('vue') || id.includes('vue-router') || id.includes('pinia')) {
                return 'vue-vendor'
              }
              // 其他 node_modules 依赖
              return 'vendor'
            }
          },
        },
      },
    },
  }
})
