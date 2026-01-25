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
  
  <!-- 文件选择器对话框 -->
  <FileSelector
    v-model="fileSelectorVisible"
    file-type="image"
    :multiple="false"
    @select="handleFileSelect"
  />
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import FileSelector from './FileSelector.vue'

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
const fileSelectorVisible = ref(false)
let savedRange = null // 保存光标位置

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
          image: function () {
            // 从文件库选择图片
            // 获取当前光标位置并保存
            const range = quill.getSelection(true)
            if (!range) {
              ElMessage.warning('请先选择插入位置')
              return
            }
            // 保存光标位置，用于后续插入图片
            savedRange = range
            fileSelectorVisible.value = true
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


// 处理文件选择
const handleFileSelect = (files) => {
  if (!files || files.length === 0) {
    return
  }
  
  // 获取第一个选中的文件（单选模式）
  const file = files[0]
  
  // 使用保存的光标位置，如果没有则尝试获取当前光标位置
  let range = savedRange
  if (!range) {
    range = quill.getSelection(true)
  }
  
  if (!range) {
    ElMessage.warning('请先选择插入位置')
    savedRange = null
    return
  }
  
  // 获取图片URL（优先使用预览URL，如果没有则使用原始URL）
  const imageUrl = file.previewUrl || file.fileUrl || ''
  
  if (!imageUrl) {
    ElMessage.error('图片URL不存在')
    savedRange = null
    return
  }
  
  // 插入图片
  quill.insertEmbed(range.index, 'image', imageUrl, 'user')
  quill.setSelection(range.index + 1)
  ElMessage.success('图片插入成功')
  
  // 清除保存的光标位置
  savedRange = null
}

// 监听禁用状态
watch(
  () => props.disabled,
  (newVal) => {
    if (newVal) {
      // 切换到禁用状态：销毁 Quill 实例
      if (quill) {
        quill = null
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
