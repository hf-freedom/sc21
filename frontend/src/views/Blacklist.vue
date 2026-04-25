<template>
  <div class="page-container">
    <div class="page-header">
      <h2>黑名单管理</h2>
      <el-button type="primary" @click="loadBlacklistedUsers">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table :data="blacklistedUsers" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="180" />
      <el-table-column prop="riskLevel" label="风险等级" width="120">
        <template #default="{ row }">
          <el-tag :type="getRiskLevelTagType(row.riskLevel)">
            {{ getRiskLevelText(row.riskLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="账户余额" width="120">
        <template #default="{ row }">
          ¥{{ row.balance }}
        </template>
      </el-table-column>
      <el-table-column prop="frozenBalance" label="冻结金额" width="120">
        <template #default="{ row }">
          ¥{{ row.frozenBalance }}
        </template>
      </el-table-column>
      <el-table-column prop="dailyTransactionCount" label="今日交易次数" width="120" />
      <el-table-column prop="dailyTransactionAmount" label="今日交易金额" width="120">
        <template #default="{ row }">
          ¥{{ row.dailyTransactionAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="consecutiveHighRiskHits" label="连续高风险次数" width="120" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="removeFromBlacklist(row.id)">
            移出黑名单
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="blacklistedUsers.length === 0" description="暂无黑名单用户" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { blacklistApi } from '../api'

const blacklistedUsers = ref([])

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

const loadBlacklistedUsers = async () => {
  try {
    const response = await blacklistApi.getBlacklistedUsers()
    if (response.data.code === 200) {
      blacklistedUsers.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('加载黑名单列表失败')
  }
}

const removeFromBlacklist = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要将该用户移出黑名单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await blacklistApi.removeFromBlacklist(userId)
    if (response.data.code === 200) {
      ElMessage.success('已将用户移出黑名单')
      loadBlacklistedUsers()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadBlacklistedUsers()
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
