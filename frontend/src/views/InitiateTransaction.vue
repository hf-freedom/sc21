<template>
  <div class="page-container">
    <div class="page-header">
      <h2>发起交易</h2>
    </div>

    <el-card class="form-card">
      <el-form :model="transactionForm" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="transactionForm.userId"
            placeholder="请选择用户"
            style="width: 300px"
            filterable
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="`${user.username} (余额: ¥${user.balance})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="交易金额" prop="amount">
          <el-input-number
            v-model="transactionForm.amount"
            :min="0.01"
            :precision="2"
            :step="100"
            style="width: 300px"
          />
        </el-form-item>

        <el-form-item label="交易类型" prop="type">
          <el-select v-model="transactionForm.type" placeholder="请选择交易类型" style="width: 300px">
            <el-option label="转账" value="TRANSFER" />
            <el-option label="提现" value="WITHDRAWAL" />
            <el-option label="充值" value="DEPOSIT" />
            <el-option label="支付" value="PAYMENT" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitTransaction" :loading="loading">
            提交交易
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog
      v-model="showResultDialog"
      title="交易结果"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="请求号">
          {{ result.requestNo }}
        </el-descriptions-item>
        <el-descriptions-item label="交易状态">
          <el-tag :type="getStatusTagType(result.status)">
            {{ getStatusText(result.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易金额">
          ¥{{ result.amount }}
        </el-descriptions-item>
        <el-descriptions-item label="交易类型">
          {{ getTransactionTypeText(result.type) }}
        </el-descriptions-item>
        <el-descriptions-item label="命中规则" v-if="result.hitRecords && result.hitRecords.length > 0">
          <div v-for="(record, index) in result.hitRecords" :key="index" class="hit-record">
            <el-tag size="small" :type="getRiskLevelTagType(record.riskLevel)" style="margin-right: 8px">
              {{ getRiskLevelText(record.riskLevel) }}
            </el-tag>
            <strong>{{ record.ruleName }}</strong>: {{ record.hitDetails }}
            <el-tag size="small" :type="getActionTagType(record.actionType)" style="margin-left: 8px">
              {{ getActionText(record.actionType) }}
            </el-tag>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="showResultDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, transactionApi } from '../api'

const users = ref([])
const formRef = ref(null)
const loading = ref(false)
const showResultDialog = ref(false)
const result = ref({})

const transactionForm = reactive({
  userId: null,
  amount: 0.01,
  type: ''
})

const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入交易金额', trigger: 'blur' }],
  type: [{ required: true, message: '请选择交易类型', trigger: 'change' }]
}

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

const loadUsers = async () => {
  try {
    const response = await userApi.getAllUsers()
    if (response.data.code === 200) {
      users.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const submitTransaction = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await transactionApi.initiateTransaction({
          userId: transactionForm.userId,
          amount: transactionForm.amount,
          type: transactionForm.type
        })
        if (response.data.code === 200) {
          result.value = response.data.data
          showResultDialog.value = true
          ElMessage.success('交易提交成功')
          loadUsers()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '交易提交失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  transactionForm.userId = null
  transactionForm.amount = 0.01
  transactionForm.type = ''
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.page-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.form-card {
  max-width: 600px;
}

.hit-record {
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
