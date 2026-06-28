/**
 * 分页组合式函数
 * 统一各页面的分页逻辑
 */
import { ref, reactive } from 'vue'

export interface PaginationState {
  pageNum: number
  pageSize: number
}

export function usePagination(defaultPageSize = 10) {
  const state = reactive<PaginationState>({
    pageNum: 1,
    pageSize: defaultPageSize
  })

  const total = ref(0)
  const loading = ref(false)

  function reset() {
    state.pageNum = 1
  }

  function goPage(p: number) {
    state.pageNum = p
  }

  function changeSize(s: number) {
    state.pageSize = s
    state.pageNum = 1
  }

  return { state, total, loading, reset, goPage, changeSize }
}
