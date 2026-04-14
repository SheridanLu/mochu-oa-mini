import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface MenuItem {
  id: number
  permissionName: string
  path: string
  icon: string
  parentId: number
  permissionType: number
  children?: MenuItem[]
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref<MenuItem[]>([])
  const isLoaded = ref(false)

  function setMenus(newMenus: MenuItem[]) {
    menus.value = newMenus
    isLoaded.value = true
  }

  function clearMenus() {
    menus.value = []
    isLoaded.value = false
  }

  return {
    menus,
    isLoaded,
    setMenus,
    clearMenus
  }
})