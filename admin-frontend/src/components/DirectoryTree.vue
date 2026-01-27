<template>
  <div class="directory-tree-container">
    <div class="tree-header">
      <el-button type="primary" size="small" :icon="Plus" @click="handleCreateDirectory">
        创建目录
      </el-button>
      <el-button size="small" :icon="Refresh" @click="handleRefresh" :loading="loading">
        刷新
      </el-button>
    </div>
    
    <div v-loading="loading" class="tree-content">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        node-key="id"
        :default-expand-all="false"
        :highlight-current="true"
        :current-node-key="selectedDirectoryId"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <el-icon><Folder /></el-icon>
            <span class="node-label">{{ node.label }}</span>
            <span class="node-actions" @click.stop>
              <el-button
                type="danger"
                link
                size="small"
                :icon="Delete"
                @click="handleDeleteDirectory(data, node)"
                title="删除目录"
              />
            </span>
          </span>
        </template>
      </el-tree>
      
      <div v-if="!loading && treeData.length === 0" class="empty-tree">
        <el-empty description="暂无目录" :image-size="80" />
      </div>
    </div>
    
    <!-- 创建目录对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建目录"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="目录类型">
          <el-radio-group v-model="createForm.isTopLevel">
            <el-radio :label="false">子目录（在当前选中目录下创建）</el-radio>
            <el-radio :label="true">顶级目录</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目录名称" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="请输入目录名称"
            maxlength="100"
            show-word-limit
            clearable
          />
          <div class="form-tip">目录名称只能包含中文、英文、数字、下划线、连字符和空格</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleCancelCreate">取消</el-button>
        <el-button type="primary" @click="handleConfirmCreate" :loading="creating">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Folder, Delete, Refresh } from '@element-plus/icons-vue'
import { getDirectoryTree, createDirectory, deleteDirectory } from '@/api/file'

const props = defineProps({
  selectedDirectoryId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['select', 'refresh'])

const loading = ref(false)
const creating = ref(false)
const deleting = ref(false)
const treeRef = ref(null)
const treeData = ref([])
const createDialogVisible = ref(false)
const createFormRef = ref(null)

const treeProps = {
  children: 'children',
  label: 'name',
}

const createForm = reactive({
  name: '',
  isTopLevel: false, // 是否创建顶级目录
})

const createRules = {
  name: [
    { required: true, message: '请输入目录名称', trigger: 'blur' },
    { min: 1, max: 100, message: '目录名称长度在 1 到 100 个字符', trigger: 'blur' },
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_\-\s]+$/,
      message: '目录名称只能包含中文、英文、数字、下划线、连字符和空格',
      trigger: 'blur',
    },
  ],
}

// 转换目录数据为树形结构
const convertToTreeData = (directories) => {
  if (!directories || directories.length === 0) {
    return []
  }
  
  return directories.map(dir => ({
    id: dir.id,
    name: dir.name,
    path: dir.path,
    parentId: dir.parentId,
    level: dir.level,
    children: dir.children && dir.children.length > 0 ? convertToTreeData(dir.children) : [],
  }))
}

// 加载目录树
const loadTree = async () => {
  loading.value = true
  try {
    const res = await getDirectoryTree()
    if (res && res.code === 200) {
      treeData.value = convertToTreeData(res.data || [])
    } else {
      ElMessage.error(res?.message || '加载目录树失败')
      treeData.value = []
    }
  } catch (error) {
    console.error('加载目录树失败:', error)
    ElMessage.error('加载目录树失败')
    treeData.value = []
  } finally {
    loading.value = false
  }
}

// 节点点击事件
const handleNodeClick = (data) => {
  emit('select', data)
}

// 打开创建目录对话框
const handleCreateDirectory = () => {
  createForm.name = ''
  // 如果没有选中目录，默认创建顶级目录
  createForm.isTopLevel = !props.selectedDirectoryId
  createDialogVisible.value = true
}

// 取消创建
const handleCancelCreate = () => {
  createDialogVisible.value = false
  createForm.name = ''
  createForm.isTopLevel = false
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
}

// 刷新目录树
const handleRefresh = async () => {
  await loadTree()
  ElMessage.success('刷新成功')
}

// 确认创建
const handleConfirmCreate = async () => {
  if (!createFormRef.value) {
    return
  }
  
  await createFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    
    // 确定父目录ID：如果是顶级目录，则为 null；否则使用选中的目录ID
    const parentId = createForm.isTopLevel ? null : props.selectedDirectoryId
    
    // 如果选择创建子目录但没有选中目录，提示用户
    if (!createForm.isTopLevel && !parentId) {
      ElMessage.warning('请先选择一个目录，或选择创建顶级目录')
      return
    }
    
    creating.value = true
    try {
      const res = await createDirectory(createForm.name, parentId)
      if (res && res.code === 200) {
        ElMessage.success('创建目录成功')
        createDialogVisible.value = false
        createForm.name = ''
        createForm.isTopLevel = false
        // 刷新目录树
        await loadTree()
        // 选中新创建的目录
        if (res.data && res.data.id) {
          emit('select', res.data)
        }
        emit('refresh')
      } else {
        ElMessage.error(res?.message || '创建目录失败')
      }
    } catch (error) {
      console.error('创建目录失败:', error)
      const errorMessage = error?.response?.data?.message || error?.message || '创建目录失败'
      ElMessage.error(errorMessage)
    } finally {
      creating.value = false
    }
  })
}

// 删除目录
const handleDeleteDirectory = async (data, node) => {
  // 检查是否有子目录
  const hasChildren = data.children && data.children.length > 0
  
  try {
    await ElMessageBox.confirm(
      hasChildren
        ? `目录 "${data.name}" 包含子目录，删除后子目录也将被删除。确定要删除吗？`
        : `确定要删除目录 "${data.name}" 吗？`,
      '删除目录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
      }
    )
    
    deleting.value = true
    try {
      const res = await deleteDirectory(data.id)
      if (res && res.code === 200) {
        ElMessage.success('删除目录成功')
        // 如果删除的是当前选中的目录，清空选中状态
        if (props.selectedDirectoryId === data.id) {
          emit('select', null)
        }
        // 刷新目录树
        await loadTree()
        emit('refresh')
      } else {
        ElMessage.error(res?.message || '删除目录失败')
      }
    } catch (error) {
      console.error('删除目录失败:', error)
      // 处理后端返回的错误信息
      const errorMessage = error?.response?.data?.message || error?.message || '删除目录失败'
      ElMessage.error(errorMessage)
    } finally {
      deleting.value = false
    }
  } catch (error) {
    // 用户取消删除
    if (error !== 'cancel') {
      console.error('删除目录确认失败:', error)
    }
  }
}

// 监听选中目录变化，更新树节点选中状态
watch(
  () => props.selectedDirectoryId,
  (newId) => {
    if (treeRef.value && newId) {
      treeRef.value.setCurrentKey(newId)
    }
  }
)

onMounted(() => {
  loadTree()
})

// 暴露方法供父组件调用
defineExpose({
  loadTree,
})
</script>

<style scoped>
.directory-tree-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-header {
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  gap: 8px;
  align-items: center;
}

.tree-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  width: 100%;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-actions {
  display: none;
  margin-left: auto;
  padding-left: 8px;
}

:deep(.el-tree-node__content:hover) .node-actions {
  display: inline-flex;
  align-items: center;
}

.empty-tree {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-tree-node__content) {
  height: 32px;
  line-height: 32px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #F5F7FA;
}
</style>
