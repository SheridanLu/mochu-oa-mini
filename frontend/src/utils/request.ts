/**
 * 与 `@/api` 共用同一 axios 实例与拦截器，避免公告等用 api、系统页用 request 时出现鉴权/提示不一致。
 */
export { request } from '@/api'
