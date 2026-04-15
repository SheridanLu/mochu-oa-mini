import { defineStore } from 'pinia'
import { ref } from 'vue'

interface UserInfo {
  id: number
  username: string
  realName: string
  avatar: string
  department: string
  roles: string[]
  mustChangePassword?: boolean
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  const USER_INFO_KEY = 'userInfo'
  const savedUserInfo = localStorage.getItem(USER_INFO_KEY)
  if (savedUserInfo) {
    try {
      userInfo.value = JSON.parse(savedUserInfo) as UserInfo
    } catch {
      localStorage.removeItem(USER_INFO_KEY)
    }
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }

  function setPermissions(perms: string[]) {
    permissions.value = perms
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem(USER_INFO_KEY)
  }

  return {
    token,
    userInfo,
    permissions,
    setToken,
    setUserInfo,
    setPermissions,
    logout
  }
})