import request from './request'

/** 上传文件，返回文件 URL */
export function uploadFile(file: File, biz: string = 'common'): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('biz', biz)
  return request.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}
