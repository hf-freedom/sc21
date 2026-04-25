<template>
  <div class="page-container">
    <div class="page-header">
      <h2>交易记录</h2>
      <el-button type="primary" @click="loadTransactions">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table :data="transactions" border stripe style="width: 100%">
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
      <el-table-column prop="status" label="交易状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见" min-width="150" />
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="reviewedAt" label="审核时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.reviewedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewHitRecords(row.id)">
            命中详情
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
        <el-table-column prop="hitTime" label="命中时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.hitTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { transactionApi } from '../api'

const transactions = ref([])
const hitRecords = ref([])
const showHitRecordsDialog = ref(false)

const getTransactionTypeText = (type) => {
  const map = {
    TRANSFER: '转账',
    WITHDRAWAL: '提现',
    DEPOSIT: '充值',
    PAYMENT: '支付'
  }
  return map[type] || type
}

const getStatusTagType = (status) => {
  switch (status) {
    case 'PENDING': return 'info'
    case 'APPROVED': return 'primary'
    case 'FROZEN': return 'warning'
    case 'REJECTED': return 'danger'
    case 'BLACKLISTED': return 'danger'
    case 'COMPLETED': return 'success'
    case 'TIMEOUT_REJECTED': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '处理中'
    case 'APPROVED': return '已通过'
    case 'FROZEN': return '冻结待审'
    case 'REJECTED': return '已拒绝'
    case 'BLACKLISTED': return '黑名单'
    case 'COMPLETED': return '已完成'
    case 'TIMEOUT_REJECTED': return '超时拒绝'
    default: return status
  }
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

const loadTransactions = async () => {
  try {
    const response = await transactionApi.getAllTransactions()
    if (response.data.code === 200) {
      transactions.value = response.data.data.sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
      )
    }
  } catch (error) {
    ElMessage.error('加载交易记录失败')
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

onMounted(() => {
  loadTransactions()
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
</style>
