import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8003/api',
  timeout: 10000
})

export const userApi = {
  getAllUsers: () => api.get('/users'),
  getUserById: (id) => api.get(`/users/${id}`),
  createUser: (user) => api.post('/users', user),
  updateUser: (id, user) => api.put(`/users/${id}`, user),
  deleteUser: (id) => api.delete(`/users/${id}`)
}

export const ruleApi = {
  getAllRules: () => api.get('/rules'),
  getRuleById: (id) => api.get(`/rules/${id}`),
  createRule: (rule) => api.post('/rules', rule),
  updateRule: (id, rule) => api.put(`/rules/${id}`, rule),
  deleteRule: (id) => api.delete(`/rules/${id}`),
  toggleRule: (id) => api.post(`/rules/${id}/toggle`)
}

export const transactionApi = {
  getAllTransactions: () => api.get('/transactions'),
  getTransactionById: (id) => api.get(`/transactions/${id}`),
  getPendingReviewTransactions: () => api.get('/transactions/pending-review'),
  initiateTransaction: (data) => api.post('/transactions/initiate', data),
  approveTransaction: (id, comment) => api.post(`/transactions/${id}/approve`, { comment }),
  rejectTransaction: (id, comment) => api.post(`/transactions/${id}/reject`, { comment }),
  getHitRecords: (id) => api.get(`/transactions/${id}/hit-records`)
}

export const blacklistApi = {
  getBlacklistedUsers: () => api.get('/blacklist'),
  addToBlacklist: (userId) => api.post(`/blacklist/${userId}`),
  removeFromBlacklist: (userId) => api.delete(`/blacklist/${userId}`)
}

export const taskApi = {
  triggerTimeoutReview: () => api.post('/tasks/timeout-review/scan')
}

export default api
