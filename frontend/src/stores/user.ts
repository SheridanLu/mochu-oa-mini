import { defineStore } from 'pinia'
import { ref } from 'vue'

interface UserInfo {
  id: number
  username: string
  realName: string
  avatar: string
  department: string
  roles: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  function setPermissions(perms: string[]) {
    permissions.value = perms
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
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