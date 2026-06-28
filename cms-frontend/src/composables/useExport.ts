/**
 * Excel 导出组合式函数
 */
import { ElMessage } from 'element-plus'

export function useExport() {
  async function exportToExcel(
    rows: Record<string, unknown>[],
    sheetName: string,
    fileName: string
  ) {
    if (rows.length === 0) {
      ElMessage.warning('当前无数据可导出')
      return
    }
    try {
      const XLSX = await import('xlsx')
      const ws = XLSX.utils.json_to_sheet(rows)
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, sheetName)
      XLSX.writeFile(wb, `${fileName}_${Date.now()}.xlsx`)
      ElMessage.success('导出成功')
    } catch {
      ElMessage.error('导出失败，请重试')
    }
  }

  return { exportToExcel }
}
