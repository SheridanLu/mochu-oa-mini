<template>
  <div class="announcement-container">
    <div class="page-header">
      <div class="header-left">
        <h2>公告管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>公告管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新建公告
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="标题">
          <el-input v-model="filterForm.title" placeholder="搜索标题" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="草稿" value="draft" />
            <el-option label="待审批" value="pending" />
            <el-option label="审批中" value="approving" />
            <el-option label="已发布" value="published" />
            <el-option label="已下线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="title" label="公告标题" min-width="200">
          <template #default="{ row }">
            <div class="announcement-title" @click="handleView(row)">
              <el-icon v-if="row.isTop" color="#f56c6c"><Top /></el-icon>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="coverImage" label="封面" width="80">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" fit="cover" style="width: 50px; height: 50px" preview-src-list :preview-teleported="true" />
            <span v-else class="no-image">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.expireTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="发布人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="canEdit(row)" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canSubmit(row)" type="primary" link @click="handleSubmit(row)">提交审批</el-button>
            <el-button v-if="canPublish(row)" type="primary" link @click="handlePublish(row)">发布</el-button>
            <el-button v-if="canOffline(row)" type="primary" link @click="handleOffline(row)">下线</el-button>
            <el-button v-if="canDelete(row)" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="formDialogVisible" :title="formTitle" width="900px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="封面图片" prop="coverImage">
          <el-upload
            v-model:file-list="coverImageList"
            action="/api/v1/attachment/upload"
            list-type="picture-card"
            :limit="1"
            :on-success="handleCoverSuccess"
            :on-remove="handleCoverRemove"
            :on-preview="handleImagePreview"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="内容正文" prop="content">
          <div class="editor-container">
            <textarea v-model="form.content" placeholder="请输入公告内容" style="width: 100%; min-height: 300px; padding: 10px; border: 1px solid #dcdfe6; border-radius: 4px;" />
          </div>
        </el-form-item>
        <el-form-item label="内容图片">
          <el-upload
            v-model:file-list="contentImageList"
            action="/api/v1/attachment/upload"
            list-type="picture-card"
            multiple
            :on-success="handleContentImageSuccess"
            :on-remove="handleContentImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            placeholder="选择过期时间（不设置则永久有效）"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button @click="handleSaveDraft" :loading="saveLoading">保存草稿</el-button>
        <el-button v-if="form.status === 'draft'" type="primary" @click="handleSubmitForm" :loading="submitLoading">提交审批</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="公告详情" width="800px">
      <div class="announcement-detail">
        <h2>{{ currentRow?.title }}</h2>
        <div class="detail-meta">
          <span>发布人：{{ currentRow?.creatorName }}</span>
          <span>发布时间：{{ formatDateTime(currentRow?.publishTime) }}</span>
        </div>
        <el-image v-if="currentRow?.coverImage" :src="currentRow?.coverImage" fit="contain" style="width: 100%; max-height: 300px; margin: 20px 0" />
        <div class="detail-content">{{ currentRow?.content }}</div>
        <div v-if="currentRow?.contentImages?.length" class="detail-images">
          <el-image v-for="img in currentRow.contentImages" :src="img" fit="contain" :preview-src-list="currentRow.contentImages" style="width: 200px; height: 150px; margin: 10px" />
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="600px">
      <el-image :src="previewImage" style="width: 100%" />
    </el-dialog>

    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <p>确定要删除这条公告吗？删除后无法恢复。</p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleDeleteConfirm" :loading="deleteLoading">确定删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { formatDateTime } from '../../../utils/format'
import api from '../../../api'

const STORAGE_KEY = 'announcement_'

const loading = ref(false)
const tableData = ref<any[]>([])
const filterForm = reactive({ title: '', status: '', dateRange: [] as string[] })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const formDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const imagePreviewVisible = ref(false)
const previewImage = ref('')
const saveLoading = ref(false)
const submitLoading = ref(false)
const deleteLoading = ref(false)
const formRef = ref<FormInstance>()
const currentRow = ref<any>(null)
const deleteId = ref(0)

const form = reactive({
  id: 0,
  title: '',
  content: '',
  coverImageId: 0,
  coverImage: '',
  contentImages: [] as string[],
  isTop: false,
  expireTime: '',
  status: 'draft'
})
const coverImageList = ref<any[]>([])
const contentImageList = ref<any[]>([])

const formRules: FormRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const formTitle = computed(() => form.id > 0 ? '编辑公告' : '新建公告')

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    draft: 'info',
    pending: 'warning',
    approving: 'warning',
    approved: 'success',
    published: 'success',
    offline: 'info',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    draft: '草稿',
    pending: '待审批',
    approving: '审批中',
    approved: '已通过',
    published: '已发布',
    offline: '已下线',
    rejected: '已驳回'
  }
  return texts[status] || status
}

const canEdit = (row: any) => ['draft', 'rejected'].includes(row.status)
const canSubmit = (row: any) => row.status === 'draft'
const canPublish = (row: any) => row.status === 'approved'
const canOffline = (row: any) => row.status === 'published'
const canDelete = (row: any) => ['draft', 'offline'].includes(row.status)

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      title: filterForm.title,
      status: filterForm.status,
      startTime: filterForm.dateRange?.[0],
      endTime: filterForm.dateRange?.[1],
      page: pagination.page,
      size: pagination.size
    }
    const res = await api.announcement.list(params)
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { filterForm.title = ''; filterForm.status = ''; filterForm.dateRange = []; pagination.page = 1; fetchList() }
const handleRefresh = () => { fetchList(); ElMessage.success('刷新成功') }

const handleCreate = () => {
  form.id = 0
  form.title = ''
  form.content = ''
  form.coverImageId = 0
  form.coverImage = ''
  form.contentImages = []
  form.isTop = false
  form.expireTime = ''
  form.status = 'draft'
  coverImageList.value = []
  contentImageList.value = []
  formDialogVisible.value = true
}

const handleEdit = async (row: any) => {
  try {
    const res = await api.announcement.get(row.id)
    if (res.code === 200) {
      const data = res.data
      form.id = data.id
      form.title = data.title
      form.content = data.content
      form.coverImageId = data.coverImageId || 0
      form.coverImage = data.coverImage || ''
      form.contentImages = data.contentImages || []
      form.isTop = data.isTop || false
      form.expireTime = data.expireTime || ''
      form.status = data.status
      coverImageList.value = data.coverImage ? [{ name: data.coverImage, url: data.coverImage }] : []
      contentImageList.value = (data.contentImages || []).map((url: string, i: number) => ({ name: `img${i}`, url }))
      formDialogVisible.value = true
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取详情失败')
  }
}

const handleView = (row: any) => {
  currentRow.value = row
  viewDialogVisible.value = true
}

const handleSaveDraft = async () => {
  if (!formRef.value) return
  await formRef.value.validateField('title').catch(() => {})
  saveLoading.value = true
  try {
    const submitData = {
      ...form,
      status: 'draft'
    }
    if (form.id > 0) {
      await api.announcement.update(submitData)
    } else {
      await api.announcement.create(submitData)
    }
    ElMessage.success('保存草稿成功')
    formDialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

const handleSubmitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id > 0) {
      await api.announcement.submit(form.id)
    }
    ElMessage.success('提交审批成功，请等待审批')
    formDialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '提交审批失败')
  } finally {
    submitLoading.value = false
  }
}

const handleSubmit = async (row: any) => {
  try {
    await api.announcement.submit(row.id)
    ElMessage.success('提交审批成功')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  }
}

const handlePublish = async (row: any) => {
  try {
    await api.announcement.publish(row.id)
    ElMessage.success('发布成功')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '发布失败')
  }
}

const handleOffline = async (row: any) => {
  try {
    await api.announcement.offline(row.id)
    ElMessage.success('已下线')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleDelete = (row: any) => {
  deleteId.value = row.id
  deleteDialogVisible.value = true
}

const handleDeleteConfirm = async () => {
  deleteLoading.value = true
  try {
    await api.announcement.delete(deleteId.value)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  } finally {
    deleteLoading.value = false
  }
}

const handleCoverSuccess = (response: any, file: any) => {
  form.coverImageId = response.data?.id || 0
  form.coverImage = response.data?.previewUrl || file.url
}

const handleCoverRemove = () => {
  form.coverImageId = 0
  form.coverImage = ''
}

const handleContentImageSuccess = (response: any, file: any) => {
  form.contentImages.push(response.data?.previewUrl || file.url)
}

const handleContentImageRemove = (file: any) => {
  const url = file.url || file.response?.data?.previewUrl
  const idx = form.contentImages.indexOf(url)
  if (idx > -1) form.contentImages.splice(idx, 1)
}

const handleImagePreview = (file: any) => {
  previewImage.value = file.url
  imagePreviewVisible.value = true
}

const handleSizeChange = (size: number) => { pagination.size = size; fetchList() }
const handleCurrentChange = (page: number) => { pagination.page = page; fetchList() }

onMounted(() => { fetchList() })
</script>

<style scoped>
.announcement-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.announcement-title { display: flex; align-items: center; gap: 4px; cursor: pointer; color: #409eff; }
.announcement-title:hover { text-decoration: underline; }
.no-image { color: #909399; font-size: 12px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
.announcement-detail h2 { margin: 0 0 16px; font-size: 20px; }
.detail-meta { display: flex; gap: 20px; color: #909399; margin-bottom: 16px; }
.detail-content { margin-top: 20px; line-height: 1.8; white-space: pre-wrap; }
.detail-images { margin-top: 20px; }
</style>