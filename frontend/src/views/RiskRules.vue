<template>
  <div class="page-container">
    <div class="page-header">
      <h2>风控规则配置</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增规则
      </el-button>
    </div>

    <el-table :data="rules" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="规则名称" width="200" />
      <el-table-column prop="ruleType" label="规则类型" width="150">
        <template #default="{ row }">
          {{ getRuleTypeText(row.ruleType) }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="100" />
      <el-table-column prop="riskLevel" label="风险等级" width="120">
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
      <el-table-column prop="thresholdAmount" label="金额阈值" width="120">
        <template #default="{ row }">
          {{ row.thresholdAmount ? '¥' + row.thresholdAmount : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="thresholdCount" label="次数阈值" width="100">
        <template #default="{ row }">
          {{ row.thresholdCount || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="timeWindowMinutes" label="时间窗口(分钟)" width="120">
        <template #default="{ row }">
          {{ row.timeWindowMinutes || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            @change="toggleRule(row.id)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="editRule(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteRule(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="showAddDialog"
      title="新增风控规则"
      width="600px"
    >
      <el-form :model="newRule" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="newRule.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="newRule.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option label="单笔金额" value="SINGLE_AMOUNT" />
            <el-option label="短时间频次" value="SHORT_TERM_FREQUENCY" />
            <el-option label="当日累计金额" value="DAILY_TOTAL_AMOUNT" />
            <el-option label="连续失败次数" value="CONSECUTIVE_FAILURES" />
            <el-option label="黑名单" value="BLACKLIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="newRule.priority" :min="0" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="newRule.riskLevel" placeholder="请选择风险等级" style="width: 100%">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理动作">
          <el-select v-model="newRule.actionType" placeholder="请选择处理动作" style="width: 100%">
            <el-option label="放行" value="APPROVE" />
            <el-option label="冻结待审" value="FREEZE" />
            <el-option label="直接拒绝" value="REJECT" />
            <el-option label="加入黑名单" value="BLACKLIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额阈值" v-if="['SINGLE_AMOUNT', 'DAILY_TOTAL_AMOUNT'].includes(newRule.ruleType)">
          <el-input-number v-model="newRule.thresholdAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="次数阈值" v-if="['SHORT_TERM_FREQUENCY', 'CONSECUTIVE_FAILURES'].includes(newRule.ruleType)">
          <el-input-number v-model="newRule.thresholdCount" :min="1" />
        </el-form-item>
        <el-form-item label="时间窗口(分钟)" v-if="newRule.ruleType === 'SHORT_TERM_FREQUENCY'">
          <el-input-number v-model="newRule.timeWindowMinutes" :min="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newRule.description" type="textarea" :rows="3" placeholder="请输入规则描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addRule">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showEditDialog"
      title="编辑风控规则"
      width="600px"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="editForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="editForm.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option label="单笔金额" value="SINGLE_AMOUNT" />
            <el-option label="短时间频次" value="SHORT_TERM_FREQUENCY" />
            <el-option label="当日累计金额" value="DAILY_TOTAL_AMOUNT" />
            <el-option label="连续失败次数" value="CONSECUTIVE_FAILURES" />
            <el-option label="黑名单" value="BLACKLIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="editForm.priority" :min="0" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="editForm.riskLevel" placeholder="请选择风险等级" style="width: 100%">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理动作">
          <el-select v-model="editForm.actionType" placeholder="请选择处理动作" style="width: 100%">
            <el-option label="放行" value="APPROVE" />
            <el-option label="冻结待审" value="FREEZE" />
            <el-option label="直接拒绝" value="REJECT" />
            <el-option label="加入黑名单" value="BLACKLIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额阈值" v-if="['SINGLE_AMOUNT', 'DAILY_TOTAL_AMOUNT'].includes(editForm.ruleType)">
          <el-input-number v-model="editForm.thresholdAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="次数阈值" v-if="['SHORT_TERM_FREQUENCY', 'CONSECUTIVE_FAILURES'].includes(editForm.ruleType)">
          <el-input-number v-model="editForm.thresholdCount" :min="1" />
        </el-form-item>
        <el-form-item label="时间窗口(分钟)" v-if="editForm.ruleType === 'SHORT_TERM_FREQUENCY'">
          <el-input-number v-model="editForm.timeWindowMinutes" :min="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入规则描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateRule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { ruleApi } from '../api'

const rules = ref([])
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const newRule = ref({
  name: '',
  ruleType: '',
  priority: 1,
  riskLevel: 'LOW',
  actionType: 'APPROVE',
  thresholdAmount: null,
  thresholdCount: null,
  timeWindowMinutes: null,
  enabled: true,
  description: ''
})
const editForm = ref({})

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

const loadRules = async () => {
  try {
    const response = await ruleApi.getAllRules()
    if (response.data.code === 200) {
      rules.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('加载规则列表失败')
  }
}

const toggleRule = async (id) => {
  try {
    const response = await ruleApi.toggleRule(id)
    if (response.data.code === 200) {
      ElMessage.success('规则状态更新成功')
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('更新规则状态失败')
  }
}

const addRule = async () => {
  if (!newRule.value.name) {
    ElMessage.warning('请输入规则名称')
    return
  }
  if (!newRule.value.ruleType) {
    ElMessage.warning('请选择规则类型')
    return
  }
  try {
    const response = await ruleApi.createRule(newRule.value)
    if (response.data.code === 200) {
      ElMessage.success('规则创建成功')
      showAddDialog.value = false
      newRule.value = {
        name: '',
        ruleType: '',
        priority: 1,
        riskLevel: 'LOW',
        actionType: 'APPROVE',
        thresholdAmount: null,
        thresholdCount: null,
        timeWindowMinutes: null,
        enabled: true,
        description: ''
      }
      loadRules()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('创建规则失败')
  }
}

const editRule = (rule) => {
  editForm.value = { ...rule }
  showEditDialog.value = true
}

const updateRule = async () => {
  try {
    const response = await ruleApi.updateRule(editForm.value.id, editForm.value)
    if (response.data.code === 200) {
      ElMessage.success('规则更新成功')
      showEditDialog.value = false
      loadRules()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('更新规则失败')
  }
}

const deleteRule = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await ruleApi.deleteRule(id)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadRules()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadRules()
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
