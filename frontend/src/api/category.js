import api from './index'

/** 获取分类树 */
export function getCategories() {
  return api.get('/categories')
}
