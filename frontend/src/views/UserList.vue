<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户列表</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <el-table :data="users" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="180" />
      <el-table-column prop="riskLevel" label="风险等级" width="120">
        <template #default="{ row }">
          <el-tag :type="getRiskLevelTagType(row.riskLevel)">
            {{ getRiskLevelText(row.riskLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="blacklisted" label="黑名单状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.blacklisted ? 'danger' : 'success'">
            {{ row.blacklisted ? '已拉黑' : '正常' }}
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
      <el-table-column prop="consecutiveFailures" label="连续失败次数" width="120" />
      <el-table-column prop="consecutiveHighRiskHits" label="连续高风险次数" width="120" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="editUser(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteUser(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="showAddDialog"
      title="新增用户"
      width="500px"
    >
      <el-form :model="newUser" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="newUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="初始余额">
          <el-input-number v-model="newUser.balance" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addUser">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showEditDialog"
      title="编辑用户"
      width="500px"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="editForm.riskLevel" placeholder="请选择风险等级">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户余额">
          <el-input-number v-model="editForm.balance" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { userApi } from '../api'

const users = ref([])
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const newUser = ref({
  username: '',
  balance: 0
})
const editForm = ref({})

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

const addUser = async () => {
  if (!newUser.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    const response = await userApi.createUser(newUser.value)
    if (response.data.code === 200) {
      ElMessage.success('用户创建成功')
      showAddDialog.value = false
      newUser.value = { username: '', balance: 0 }
      loadUsers()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('创建用户失败')
  }
}

const editUser = (user) => {
  editForm.value = { ...user }
  showEditDialog.value = true
}

const updateUser = async () => {
  try {
    const response = await userApi.updateUser(editForm.value.id, editForm.value)
    if (response.data.code === 200) {
      ElMessage.success('用户更新成功')
      showEditDialog.value = false
      loadUsers()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('更新用户失败')
  }
}

const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await userApi.deleteUser(id)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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
