/** 规范站内资源地址，开发环境经 Vite `/api` 代理访问后端 */
export function resolveMediaUrl(url: string | null | undefined): string {
  if (url == null || url === '') return ''
  const u = String(url).trim()
  if (/^https?:\/\//i.test(u)) return u
  if (u.startsWith('//')) return u
  return u.startsWith('/') ? u : `/${u}`
}

/** 公告等内容图：库内多为 JSON 文本 */
export function parseJsonStringArray(val: unknown): string[] {
  if (val == null) return []
  if (Array.isArray(val)) return val.map(String)
  if (typeof val === 'string') {
    const s = val.trim()
    if (!s) return []
    try {
      const v = JSON.parse(s)
      return Array.isArray(v) ? v.map(String) : []
    } catch {
      return []
    }
  }
  return []
}
