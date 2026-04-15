import { ElMessage } from 'element-plus'
import { resolveMediaUrl } from '@/utils/media'

export const UPLOAD_ACCEPT = {
  images: 'image/png,image/jpeg,image/jpg,image/webp,image/gif',
  docs: '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar,.jpg,.jpeg,.png,.webp',
  docsWithCad: '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar,.dwg,.dxf,.jpg,.jpeg,.png,.webp'
}

export function buildUploadHeaders() {
  const token = (localStorage.getItem('token') || '')
    .trim()
    .replace(/^"+|"+$/g, '')
    .replace(/^Bearer\s+/i, '')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function createBeforeUpload(maxSizeMB: number, allowedExts: string[]) {
  const normalized = allowedExts.map((ext) => ext.toLowerCase())
  return (file: File) => {
    const sizeOk = file.size / 1024 / 1024 <= maxSizeMB
    if (!sizeOk) {
      ElMessage.error(`文件大小不能超过 ${maxSizeMB}MB`)
      return false
    }
    const name = file.name || ''
    const ext = name.includes('.') ? `.${name.split('.').pop()!.toLowerCase()}` : ''
    if (!ext || !normalized.includes(ext)) {
      ElMessage.error(`不支持该文件格式：${ext || '未知类型'}`)
      return false
    }
    return true
  }
}

export function handleUploadExceed(limit: number) {
  ElMessage.warning(`最多只能上传 ${limit} 个文件`)
}

export function pickUploadUrl(response: any, file?: any): string {
  const d = response?.data ?? response ?? {}
  return String(d?.previewUrl || d?.url || file?.url || '')
}

export function previewUploadedFile(file: any) {
  const url = String(file?.url || file?.response?.data?.previewUrl || file?.response?.data?.url || '')
  if (!url) {
    ElMessage.info('暂无可预览地址')
    return
  }
  const lower = url.toLowerCase()
  if (lower.endsWith('.dwg') || lower.endsWith('.dxf')) {
    ElMessage.info('DWG/DXF 暂不支持在线预览，请下载后查看')
    return
  }
  window.open(resolveMediaUrl(url), '_blank')
}
