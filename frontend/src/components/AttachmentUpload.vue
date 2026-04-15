<template>
  <el-upload
    action="/api/common/upload"
    list-type="text"
    :headers="headers"
    :accept="accept"
    :limit="limit"
    :multiple="multiple"
    :auto-upload="autoUpload"
    :before-upload="beforeUpload"
    :on-exceed="() => handleUploadExceed(limit)"
    :on-preview="previewUploadedFile"
    :on-success="handleSuccess"
    v-bind="$attrs"
  >
    <slot>
      <el-button type="primary">上传附件</el-button>
    </slot>
  </el-upload>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { UPLOAD_ACCEPT, buildUploadHeaders, createBeforeUpload, handleUploadExceed, pickUploadUrl, previewUploadedFile } from '@/utils/upload'

const props = withDefaults(defineProps<{
  limit?: number
  multiple?: boolean
  autoUpload?: boolean
  maxSizeMB?: number
  allowedExts?: string[]
  accept?: string
}>(), {
  limit: 30,
  multiple: true,
  autoUpload: true,
  maxSizeMB: 20,
  allowedExts: () => ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.txt', '.zip', '.rar', '.dwg', '.dxf', '.jpg', '.jpeg', '.png', '.webp'],
  accept: UPLOAD_ACCEPT.docsWithCad
})

const emit = defineEmits<{
  uploaded: [url: string, response: any, file: any]
}>()

const headers = computed(() => buildUploadHeaders())
const beforeUpload = createBeforeUpload(props.maxSizeMB, props.allowedExts)

const handleSuccess = (response: any, file: any) => {
  const url = pickUploadUrl(response, file)
  if (url) emit('uploaded', url, response, file)
}
</script>
