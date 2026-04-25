<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待审核列表</h2>
      <div class="header-actions">
        <el-button type="warning" @click="triggerTimeoutReview">
          <el-icon><Clock /></el-icon>
          超时审核任务
        </el-button>
        <el-button type="primary" @click="loadPendingTransactions">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-table :data="pendingTransactions" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="requestNo" label="请求号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="amount" label="交易金额" width="120">
        <template #default="{ row }">
          ¥{{ row.amount }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="交易类型" width="120">
        <template #default="{ row }">
          {{ getTransactionTypeText(row.type) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="waitTime" label="等待时间" width="120">
        <template #default="{ row }">
          {{ getWaitTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewHitRecords(row.id)">
            命中详情
          </el-button>
          <el-button type="success" size="small" @click="approveTransaction(row.id)">
            审核通过
          </el-button>
          <el-button type="danger" size="small" @click="rejectTransaction(row.id)">
            审核拒绝
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="showHitRecordsDialog"
      title="规则命中详情"
      width="800px"
    >
      <el-table :data="hitRecords" border stripe style="width: 100%">
        <el-table-column prop="ruleName" label="规则名称" width="200" />
        <el-table-column prop="ruleType" label="规则类型" width="150">
          <template #default="{ row }">
            {{ getRuleTypeText(row.ruleType) }}
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelTagType(row.riskLevel)">
              {{ getRiskLevelText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actionType" label="处理动作" width="120">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.actionType)">
              {{ getActionText(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hitDetails" label="命中详情" min-width="200" />
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="showReviewDialog"
      :title="reviewAction === 'approve' ? '审核通过' : '审核拒绝'"
      width="500px"
    >
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button :type="reviewAction === 'approve' ? 'success' : 'danger'" @click="submitReview">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Clock } from '@element-plus/icons-vue'
import { transactionApi, taskApi } from '../api'

const pendingTransactions = ref([])
const hitRecords = ref([])
const showHitRecordsDialog = ref(false)
const showReviewDialog = ref(false)
const reviewAction = ref('')
const currentTransactionId = ref(null)
const reviewForm = ref({
  comment: ''
})

const getTransactionTypeText = (type) => {
  const map = {
    TRANSFER: '转账',
    WITHDRAWAL: '提现',
    DEPOSIT: '充值',
    PAYMENT: '支付'
  }
  return map[type] || type
}

const getRuleTypeText = (type) => {
  const map = {
    SINGLE_AMOUNT: '单笔金额',
    SHORT_TERM_FREQUENCY: '短时间频次',
    DAILY_TOTAL_AMOUNT: '当日累计金额',
    CONSECUTIVE_FAILURES: '连续失败次数',
    BLACKLIST: '黑名单'
  }
  return map[type] || type
}

const getRiskLevelTagType = (level) => {
  switch (level) {
    case 'LOW': return 'success'
    case 'MEDIUM': return 'warning'
    case 'HIGH': return 'danger'
    default: return 'info'
  }
}

const getRiskLevelText = (level) => {
  switch (level) {
    case 'LOW': return '低风险'
    case 'MEDIUM': return '中风险'
    case 'HIGH': return '高风险'
    default: return level
  }
}

const getActionTagType = (action) => {
  switch (action) {
    case 'APPROVE': return 'success'
    case 'FREEZE': return 'warning'
    case 'REJECT': return 'danger'
    case 'BLACKLIST': return 'danger'
    default: return 'info'
  }
}

const getActionText = (action) => {
  switch (action) {
    case 'APPROVE': return '放行'
    case 'FREEZE': return '冻结待审'
    case 'REJECT': return '直接拒绝'
    case 'BLACKLIST': return '加入黑名单'
    default: return action
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getWaitTime = (createdAt) => {
  if (!createdAt) return '-'
  const now = new Date()
  const created = new Date(createdAt)
  const diff = now - created
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时${minutes % 60}分钟`
  const days = Math.floor(hours / 24)
  return `${days}天${hours % 24}小时`
}

const loadPendingTransactions = async () => {
  try {
    const response = await transactionApi.getPendingReviewTransactions()
    if (response.data.code === 200) {
      pendingTransactions.value = response.data.data.sort((a, b) => 
        new Date(a.createdAt) - new Date(b.createdAt)
      )
    }
  } catch (error) {
    ElMessage.error('加载待审核列表失败')
  }
}

const triggerTimeoutReview = async () => {
  try {
    await ElMessageBox.confirm('确定要执行超时审核任务吗？超时未审核的交易将被自动拒绝。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await taskApi.triggerTimeoutReview()
    if (response.data.code === 200) {
      ElMessage.success('超时审核任务执行成功')
      loadPendingTransactions()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('执行超时审核任务失败')
    }
  }
}

const viewHitRecords = async (transactionId) => {
  try {
    const response = await transactionApi.getHitRecords(transactionId)
    if (response.data.code === 200) {
      hitRecords.value = response.data.data
      showHitRecordsDialog.value = true
    }
  } catch (error) {
    ElMessage.error('加载命中详情失败')
  }
}

const approveTransaction = (id) => {
  currentTransactionId.value = id
  reviewAction.value = 'approve'
  reviewForm.value.comment = '人工审核通过'
  showReviewDialog.value = true
}

const rejectTransaction = (id) => {
  currentTransactionId.value = id
  reviewAction.value = 'reject'
  reviewForm.value.comment = '人工审核拒绝'
  showReviewDialog.value = true
}

const submitReview = async () => {
  try {
    let response
    if (reviewAction.value === 'approve') {
      response = await transactionApi.approveTransaction(currentTransactionId.value, reviewForm.value.comment)
    } else {
      response = await transactionApi.rejectTransaction(currentTransactionId.value, reviewForm.value.comment)
    }
    if (response.data.code === 200) {
      ElMessage.success(reviewAction.value === 'approve' ? '审核通过成功' : '审核拒绝成功')
      showReviewDialog.value = false
      loadPendingTransactions()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('审核操作失败')
  }
}

onMounted(() => {
  loadPendingTransactions()
})
</script>

<style scoped>
.page-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
