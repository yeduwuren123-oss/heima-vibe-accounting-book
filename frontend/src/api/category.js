import api from './index'

/** 获取分类树，可按类型筛选 */
export function getCategories(params) {
  return api.get('/categories', { params })
}

/** 创建自定义分类 */
export function createCategory(data) {
  return api.post('/categories', data)
}

/** 修改分类名称 */
export function updateCategory(id, data) {
  return api.put(`/categories/${id}`, data)
}

/** 删除自定义分类 */
export function deleteCategory(id) {
  return api.delete(`/categories/${id}`)
}
