<template>
  <div class="sub-report">
    <div class="page-header">
      <el-button text @click="$router.push('/report')">← 返回概览</el-button>
      <h2>自定义报表</h2>
      <p class="desc">按业务类型与日期区间导出 CSV（UTF-8，含 BOM，便于 Excel 打开）。单次最多 {{ rowLimit }} 行。</p>
    </div>

    <el-card class="form-card" shadow="never">
      <el-form label-width="100px" :model="form" class="export-form">
        <el-form-item label="数据类型" required>
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="日常报销" value="expense" />
            <el-option label="发票" value="invoice" />
            <el-option label="成本归集" value="cost" />
            <el-option label="付款申请" value="payment_apply" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期区间" required>
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="form.projectId" placeholder="全部项目" clearable filterable style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="exporting" @click="handleExport">导出 CSV</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-alert type="info" :closable="false" class="hint" show-icon>
      <template #title>
        <span>说明：发票按「开票日期」筛选；报销、付款申请按「创建时间」；成本按「归集日期」。不选项目则导出全部符合条件的数据。</span>
      </template>
    </el-alert>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const router = useRouter()
const rowLimit = 10000
const exporting = ref(false)
const projectOptions = ref<any[]>([])

const form = reactive({
  type: 'expense',
  dateRange: [] as string[],
  projectId: undefined as number | undefined
})

/** 从 Spring / Result / Problem JSON 或纯文本/HTML 中提取可读错误信息 */
function parseExportErrorBody(text: string, status: number): string {
  const raw = (text || '').trim()
  if (!raw) return status === 401 ? '未登录或登录已过期' : `导出失败（${status}）`
  try {
    const j = JSON.parse(raw) as Record<string, unknown>
    const msg =
      typeof j.message === 'string'
        ? j.message
        : typeof j.detail === 'string'
          ? j.detail
          : typeof j.msg === 'string'
            ? j.msg
            : null
    if (msg) return msg
  } catch {
    /* 非 JSON */
  }
  if (raw.startsWith('<')) {
    const stripped = raw.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
    return stripped.length > 200 ? stripped.slice(0, 200) + '…' : stripped || `导出失败（${status}）`
  }
  return raw.length > 400 ? raw.slice(0, 400) + '…' : raw
}

function filenameFromContentDisposition(cd: string | null, fallback: string): string {
  if (!cd) return fallback
  const star = /filename\*=UTF-8''([^;\s]+)/i.exec(cd)
  if (star?.[1]) {
    try {
      return decodeURIComponent(star[1].trim())
    } catch {
      /* fall through */
    }
  }
  const m = /filename="?([^";\n]+)"?/i.exec(cd)
  if (m?.[1]) return m[1].trim()
  return fallback
}

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    projectOptions.value = res.code === 200 ? res.data || [] : []
  } catch {
    projectOptions.value = []
  }
}

const handleExport = async () => {
  if (!form.dateRange || form.dateRange.length !== 2) {
    ElMessage.warning('请选择日期区间')
    return
  }
  exporting.value = true
  try {
    const token = localStorage.getItem('token')
    const url = api.report.exportCsvQuery({
      type: form.type,
      from: form.dateRange[0],
      to: form.dateRange[1],
      projectId: form.projectId
    })
    const res = await fetch(url, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (res.status === 401) {
      localStorage.removeItem('token')
      ElMessage.error('登录已过期，请重新登录')
      await router.push('/login')
      return
    }
    if (!res.ok) {
      const text = await res.text()
      throw new Error(parseExportErrorBody(text, res.status))
    }
    const blob = await res.blob()
    const cd = res.headers.get('Content-Disposition')
    const filename = filenameFromContentDisposition(cd, `report-${form.type}.csv`)
    const href = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = href
    a.download = filename
    a.click()
    URL.revokeObjectURL(href)
    ElMessage.success('已开始下载')
  } catch (e: any) {
    ElMessage.error(e.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await loadProjects()
  const end = new Date()
  const start = new Date()
  start.setMonth(start.getMonth() - 1)
  const fmt = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  form.dateRange = [fmt(start), fmt(end)]
})
</script>

<style scoped>
.sub-report {
  padding: 20px;
  max-width: 640px;
}
.desc {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}
.form-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  margin-bottom: 16px;
}
.export-form {
  padding: 8px 0;
}
.hint {
  border-radius: 8px;
}
</style>
