<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>墨初OA施工管理系统</h1>
        <p>MOCHU-OA Construction Management System</p>
      </div>
      
      <div v-if="step === 1" class="step1">
        <el-form ref="formRef" :model="step1Form" :rules="step1Rules" class="login-form">
          <el-form-item prop="account">
            <el-input v-model="step1Form.account" placeholder="请输入用户名或手机号" size="large" :prefix-icon="User" @keyup.enter="handleNextStep" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="login-btn" @click="handleNextStep">
              下一步
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <span class="login-type" :class="{ active: loginType === 'password' }" @click="loginType = 'password'">密码登录</span>
          <span class="divider">|</span>
          <span class="login-type" :class="{ active: loginType === 'sms' }" @click="loginType = 'sms'">验证码登录</span>
        </div>
      </div>
      
      <div v-else class="step2">
        <div class="step2-header">
          <el-button link @click="step = 1">
            <el-icon><ArrowLeft /></el-icon>返回
          </el-button>
          <span class="account-display">{{ step1Form.account }}</span>
        </div>
        
        <el-form v-if="loginType === 'password'" ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="login-form">
          <el-form-item prop="password">
            <el-input v-model="passwordForm.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <span class="forgot-link" @click="showForgotDialog">忘记密码？</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        
        <el-form v-else ref="smsFormRef" :model="smsForm" :rules="smsRules" class="login-form">
          <el-form-item prop="code">
            <el-input v-model="smsForm.code" placeholder="请输入验证码" size="large" :prefix-icon="Message" @keyup.enter="handleLogin">
              <template #append>
                <el-button :disabled="smsCountdown > 0" @click="sendSmsCode">
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="login-footer" v-if="step === 2">
        <span class="other-account" @click="step = 1; loginType = loginType === 'password' ? 'sms' : 'password'">
          使用{{ loginType === 'password' ? '验证码' : '密码' }}登录
        </span>
      </div>
    </div>
    
    <el-dialog v-model="forgotDialogVisible" title="找回密码" width="400px">
      <el-steps :active="forgotStep" finish-status="success" simple>
        <el-step title="输入账号" />
        <el-step title="验证手机" />
        <el-step title="重置密码" />
      </el-steps>
      
      <div class="forgot-content">
        <el-form v-if="forgotStep === 0" ref="forgotFormRef" :model="forgotForm" :rules="forgotRules">
          <el-form-item prop="account">
            <el-input v-model="forgotForm.account" placeholder="请输入用户名或绑定的手机号" />
          </el-form-item>
        </el-form>
        
        <el-form v-if="forgotStep === 1" ref="forgotSmsFormRef" :model="forgotSmsForm">
          <el-form-item>
            <el-input v-model="forgotSmsForm.code" placeholder="请输入手机验证码">
              <template #append>
                <el-button :disabled="forgotCountdown > 0" @click="sendForgotCode">
                  {{ forgotCountdown > 0 ? `${forgotCountdown}s` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        
        <el-form v-if="forgotStep === 2" ref="resetFormRef" :model="resetForm" :rules="resetRules">
          <el-form-item prop="password">
            <el-input v-model="resetForm.password" type="password" placeholder="请输入新密码" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button v-if="forgotStep > 0" @click="forgotStep--">上一步</el-button>
        <el-button v-if="forgotStep < 2" type="primary" @click="nextForgotStep">下一步</el-button>
        <el-button v-else type="primary" :loading="resetLoading" @click="handleResetPassword">确定重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Message, ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { api } from '../../api'

const router = useRouter()
const userStore = useUserStore()

const step = ref(1)
const loginType = ref<'password' | 'sms'>('password')
const loading = ref(false)
const rememberMe = ref(false)

const step1Form = reactive({ account: '' })
const step1Rules = {
  account: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }]
}

const passwordForm = reactive({ password: '' })
const passwordRules = {
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const smsForm = reactive({ code: '' })
const smsRules = {
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

const forgotDialogVisible = ref(false)
const forgotStep = ref(0)
const forgotCountdown = ref(0)
const resetLoading = ref(false)
const forgotForm = reactive({ account: '' })
const forgotSmsForm = reactive({ code: '' })
const resetForm = reactive({ password: '', confirmPassword: '' })

const forgotRules = {
  account: [{ required: true, message: '请���入��户名或手机号', trigger: 'blur' }]
}

const resetRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== resetForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

let formRef = ref<FormInstance>()
let passwordFormRef = ref<FormInstance>()
let smsFormRef = ref<FormInstance>()
let forgotFormRef = ref<FormInstance>()
let forgotSmsFormRef = ref<FormInstance>()
let resetFormRef = ref<FormInstance>()

const handleNextStep = async () => {
  if (!step1Form.account) {
    ElMessage.warning('请输入用户名或手机号')
    return
  }
  step.value = 2
}

const sendSmsCode = async () => {
  if (smsCountdown.value > 0) return
  try {
    await api.auth.sendSms({ phone: step1Form.account, type: 'login' })
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0 && smsTimer) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
  }
}

const handleLogin = async () => {
  const form = loginType.value === 'password' ? passwordForm : smsForm
  const formRef = loginType.value === 'password' ? passwordFormRef : smsFormRef
  
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const loginData = loginType.value === 'password'
      ? { username: step1Form.account, password: passwordForm.password }
      : { phone: step1Form.account, code: smsForm.code }
    
    const res = await api.auth.login(loginData)
    
    if (res.code === 200) {
      userStore.setToken(res.data?.token || '')
      userStore.setUserInfo({
        id: res.data?.userId || 0,
        username: res.data?.username || '',
        realName: res.data?.realName || '',
        avatar: res.data?.avatar || '',
        department: res.data?.departmentName || '',
        roles: res.data?.roles || []
      })
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const showForgotDialog = () => {
  forgotDialogVisible.value = true
  forgotStep.value = 0
  forgotForm.account = step1Form.account
}

const sendForgotCode = async () => {
  if (forgotCountdown.value > 0) return
  try {
    await api.auth.sendSms({ phone: forgotForm.account, type: 'forgot' })
    ElMessage.success('验证码已发送')
    forgotCountdown.value = 60
    const timer = setInterval(() => {
      forgotCountdown.value--
      if (forgotCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
  }
}

const nextForgotStep = async () => {
  if (forgotStep.value === 0) {
    const valid = await forgotFormRef.value?.validate().catch(() => false)
    if (!valid) return
    forgotStep.value = 1
  } else if (forgotStep.value === 1) {
    if (!forgotSmsForm.code) {
      ElMessage.warning('请输入验证码')
      return
    }
    forgotStep.value = 2
  }
}

const handleResetPassword = async () => {
  const valid = await resetFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  resetLoading.value = true
  try {
    await api.auth.resetPassword({
      account: forgotForm.account,
      code: forgotSmsForm.code,
      password: resetForm.password
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    forgotDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '重置失败')
  } finally {
    resetLoading.value = false
  }
}

onUnmounted(() => {
  if (smsTimer) clearInterval(smsTimer)
})
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 24px;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
}

.login-type {
  color: #999;
  cursor: pointer;
  padding: 0 10px;
}

.login-type.active {
  color: #409eff;
  font-weight: 500;
}

.divider {
  color: #ddd;
}

.step2-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.account-display {
  color: #409eff;
  font-size: 14px;
}

.forgot-link {
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  margin-left: auto;
}

.forgot-link:hover {
  text-decoration: underline;
}

.other-account {
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}

.other-account:hover {
  text-decoration: underline;
}

.forgot-content {
  padding: 30px 0;
}
</style>