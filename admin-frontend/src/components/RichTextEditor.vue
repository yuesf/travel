<template>
  <!-- 查看模式：使用只读 div 显示内容，避免 Quill 样式问题 -->
  <div v-if="disabled" class="rich-text-editor-container is-disabled" :id="props.id || undefined">
    <div 
      class="readonly-content" 
      v-html="modelValue || `<span style='color: #c0c4cc; font-style: italic;'>${placeholder}</span>`"
    ></div>
  </div>
  <!-- 编辑模式：使用 Quill 编辑器 -->
  <div v-else class="rich-text-editor-container" :id="props.id || undefined">
    <div ref="editorRef" class="editor-wrapper"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import { API_BASE_URL } from '@/config/api'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '请输入内容...',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  id: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const editorRef = ref(null)
let quill = null

// 初始化编辑器
const initEditor = () => {
  // 如果是禁用状态，不初始化 Quill
  if (props.disabled) return
  if (!editorRef.value) return

  quill = new Quill(editorRef.value, {
    theme: 'snow',
    placeholder: props.placeholder,
    modules: {
      toolbar: {
        container: [
          [{ header: [1, 2, 3, 4, 5, 6, false] }],
          ['bold', 'italic', 'underline', 'strike'],
          [{ color: [] }, { background: [] }],
          [{ script: 'sub' }, { script: 'super' }],
          [{ list: 'ordered' }, { list: 'bullet' }],
          [{ indent: '-1' }, { indent: '+1' }],
          [{ align: [] }],
          ['link', 'image', 'video'],
          ['clean'],
        ],
        handlers: {
          image: async function () {
            // 自定义图片上传处理
            const input = document.createElement('input')
            input.setAttribute('type', 'file')
            input.setAttribute('accept', 'image/jpeg,image/jpg,image/png,image/webp')
            input.click()

            input.onchange = async () => {
              const file = input.files[0]
              if (!file) return

              // 验证文件类型
              const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)
              if (!isValidType) {
                ElMessage.error('只能上传图片文件（JPG、PNG、WebP格式）')
                return
              }

              // 验证文件大小（5MB）
              const maxSize = 5 * 1024 * 1024 // 5MB
              if (file.size > maxSize) {
                ElMessage.error('图片大小不能超过5MB')
                return
              }

              // 获取当前光标位置
              const range = quill.getSelection(true)
              if (!range) {
                ElMessage.warning('请先选择插入位置')
                return
              }

              // 插入加载占位符
              quill.insertText(range.index, '上传中...', 'user')
              quill.setSelection(range.index + 3)

              try {
                // 创建FormData
                const formData = new FormData()
                formData.append('file', file)
                formData.append('module', 'article') // 默认使用article模块

                // 构建上传URL：统一基于 API_BASE_URL
                // 开发环境：/api/v1/common/file/upload/image
                // 生产环境：/travel/api/v1/common/file/upload/image
                const baseUrl = API_BASE_URL.replace(/\/$/, '')
                const uploadUrl = `${baseUrl}/common/file/upload/image`

                // 调用上传API
                const token = getToken()
                const response = await fetch(uploadUrl, {
                  method: 'POST',
                  headers: {
                    Authorization: `Bearer ${token}`,
                  },
                  body: formData,
                })

                const result = await response.json()

                if (result.code === 200) {
                  const imageUrl = result.data?.url || result.data
                  // 删除加载占位符
                  quill.deleteText(range.index, 3)
                  // 插入图片
                  quill.insertEmbed(range.index, 'image', imageUrl, 'user')
                  quill.setSelection(range.index + 1)
                  ElMessage.success('图片上传成功')
                } else {
                  // 删除加载占位符
                  quill.deleteText(range.index, 3)
                  ElMessage.error(result.message || '图片上传失败')
                }
              } catch (error) {
                console.error('图片上传失败:', error)
                // 删除加载占位符
                quill.deleteText(range.index, 3)
                ElMessage.error('图片上传失败，请重试')
              }
            }
          },
        },
      },
    },
  })

  // 设置初始内容
  if (props.modelValue) {
    quill.root.innerHTML = props.modelValue
  }

  // 监听内容变化
  quill.on('text-change', () => {
    const content = quill.root.innerHTML
    emit('update:modelValue', content)
    emit('change', content)
  })

}

// 监听外部值变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (quill && quill.root.innerHTML !== newVal) {
      quill.root.innerHTML = newVal || ''
    }
  }
)


// 监听禁用状态
watch(
  () => props.disabled,
  (newVal) => {
    if (newVal) {
      // 切换到禁用状态：销毁 Quill 实例
      if (quill) {
        quill = null
      }
      // 停止 MutationObserver
      if (mutationObserver) {
        mutationObserver.disconnect()
        mutationObserver = null
      }
    } else {
      // 切换到启用状态：初始化 Quill
      if (!quill && editorRef.value) {
        initEditor()
      } else if (quill) {
        quill.enable(true)
      }
    }
  }
)

onMounted(() => {
  // 只有在非禁用状态下才初始化 Quill
  if (!props.disabled) {
    initEditor()
  }
})

onBeforeUnmount(() => {
  if (quill) {
    quill = null
  }
})

// 暴露方法供父组件调用
defineExpose({
  getContent: () => quill?.root.innerHTML || '',
  setContent: (content) => {
    if (quill) {
      quill.root.innerHTML = content || ''
    }
  },
  getText: () => quill?.getText() || '',
  focus: () => quill?.focus(),
})
</script>

<style scoped>
.rich-text-editor-container {
  width: 100%;
}

.editor-wrapper {
  min-height: 300px;
}

:deep(.ql-container) {
  min-height: 300px;
  font-size: 14px;
}

:deep(.ql-editor) {
  min-height: 300px;
}

:deep(.ql-editor img) {
  max-width: 100%;
  height: auto;
}

/* 禁用状态下的样式 - 完全移除阴影 */
.rich-text-editor-container.is-disabled {
  box-shadow: none !important;
}

.rich-text-editor-container.is-disabled :deep(.ql-container) {
  box-shadow: none !important;
  border-color: #dcdfe6;
  background-color: #f5f7fa;
}

.rich-text-editor-container.is-disabled :deep(.ql-container.ql-snow) {
  box-shadow: none !important;
  border: 1px solid #dcdfe6 !important;
}

.rich-text-editor-container.is-disabled :deep(.ql-toolbar) {
  display: none;
}

.rich-text-editor-container.is-disabled :deep(.ql-editor) {
  background-color: #f5f7fa;
  cursor: default;
  box-shadow: none !important;
  text-shadow: none !important;
}

/* 移除 placeholder 的阴影效果 */
.rich-text-editor-container.is-disabled :deep(.ql-editor.ql-blank::before) {
  color: #c0c4cc;
  font-style: normal;
  text-shadow: none !important;
  box-shadow: none !important;
}

.rich-text-editor-container.is-disabled :deep(.ql-editor::before) {
  color: #c0c4cc;
  font-style: normal;
  text-shadow: none !important;
  box-shadow: none !important;
}

/* 移除所有可能的阴影效果 - 使用更具体的选择器 */
.rich-text-editor-container.is-disabled :deep(.ql-container),
.rich-text-editor-container.is-disabled :deep(.ql-editor),
.rich-text-editor-container.is-disabled :deep(.ql-snow),
.rich-text-editor-container.is-disabled :deep(.ql-container.ql-snow),
.rich-text-editor-container.is-disabled :deep(.ql-editor.ql-blank) {
  box-shadow: none !important;
  text-shadow: none !important;
  filter: none !important;
}

/* 查看模式下的只读内容样式 */
.rich-text-editor-container.is-disabled .readonly-content {
  min-height: 300px;
  padding: 12px 15px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  line-height: 1.5;
  box-shadow: none !important;
  text-shadow: none !important;
}

.rich-text-editor-container.is-disabled .readonly-content :deep(*) {
  box-shadow: none !important;
  text-shadow: none !important;
}
</style>

<!-- 全局样式，用于覆盖 Quill 的全局 CSS -->
<style>
/* 查看模式下移除 Quill 编辑器的所有阴影 */
.rich-text-editor-container.is-disabled .ql-container.ql-snow {
  box-shadow: none !important;
}

.rich-text-editor-container.is-disabled .ql-editor {
  box-shadow: none !important;
  text-shadow: none !important;
}

.rich-text-editor-container.is-disabled .ql-editor::before {
  text-shadow: none !important;
  box-shadow: none !important;
}

.rich-text-editor-container.is-disabled .ql-editor.ql-blank::before {
  text-shadow: none !important;
  box-shadow: none !important;
}

/* 移除所有可能的阴影和滤镜效果 */
.rich-text-editor-container.is-disabled * {
  box-shadow: none !important;
  text-shadow: none !important;
  filter: none !important;
}
</style>
