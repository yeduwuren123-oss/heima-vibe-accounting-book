import api from './index'

/** 分页查询记录 */
export function getRecords(params) {
  return api.get('/records', { params })
}

/** 新增记录 */
export function createRecord(data) {
  return api.post('/records', data)
}

/** 更新记录 */
export function updateRecord(id, data) {
  return api.put(`/records/${id}`, data)
}

/** 删除记录 */
export function deleteRecord(id) {
  return api.delete(`/records/${id}`)
}

/** 月度统计 */
export function getMonthlyStats(year, month) {
  return api.get('/records/stats/monthly', { params: { year, month } })
}

/** 每日统计（日历视图） */
export function getDailyStats(year, month) {
  return api.get('/records/stats/daily', { params: { year, month } })
}
