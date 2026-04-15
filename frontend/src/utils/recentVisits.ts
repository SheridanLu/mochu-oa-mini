export type RecentVisit = {
  path: string
  title: string
  at: number
}

const STORAGE_KEY = 'mochu_oa_recent_visits'
const MAX_ITEMS = 12

export function pushRecentVisit(path: string, title: string) {
  if (!path || path === '/login') return
  const label = (title && title.trim()) || path
  let list: RecentVisit[] = []
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) list = JSON.parse(raw)
  } catch {
    list = []
  }
  if (!Array.isArray(list)) list = []
  list = list.filter((x) => x && x.path !== path)
  list.unshift({ path, title: label, at: Date.now() })
  if (list.length > MAX_ITEMS) list = list.slice(0, MAX_ITEMS)
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
  } catch {
    /* ignore quota */
  }
}

export function getRecentVisits(): RecentVisit[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const list = JSON.parse(raw)
    return Array.isArray(list) ? list : []
  } catch {
    return []
  }
}

export function clearRecentVisits() {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    /* ignore */
  }
}
