<template>
  <div class="review-assign">
    <el-card>
      <template #header>
        <span>待分配审核的成果</span>
      </template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="title" label="成果名称" min-width="200" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="createdBy" label="提交人" width="120" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="assignExpert(row)">
              分配专家
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="assignDialog" title="分配审核专家" width="600px">
      <el-form label-width="100px">
        <el-form-item label="选择专家">
          <el-select v-model="selectedExperts" multiple placeholder="请选择专家" v-loading="expertsLoading">
            <el-option
              v-for="expert in experts"
              :key="expert.id"
              :label="expert.name"
              :value="expert.id"
            >
              <span>{{ expert.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingAssignResults, assignReviewers } from '@/api/result'
import { getExpertUsers, type KeycloakUser } from '@/api/user'

const loading = ref(false)
const expertsLoading = ref(false)
const assignLoading = ref(false)
const tableData = ref([])
const assignDialog = ref(false)
const selectedExperts = ref([])
const currentResult = ref(null)
const experts = ref<KeycloakUser[]>([])

onMounted(() => {
  loadData()
  loadExperts()
})

async function loadData() {
  loading.value = true
  try {
    // 使用专门的API获取待分配审核的成果
    const res = await getPendingAssignResults({ page: 1, pageSize: 50 })
    tableData.value = res?.data?.list || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

async function loadExperts() {
  expertsLoading.value = true
  try {
    const res = await getExpertUsers()
    experts.value = res?.data || []
  } catch (error) {
    console.error('加载专家列表失败:', error)
    ElMessage.error('加载专家列表失败')
  } finally {
    expertsLoading.value = false
  }
}

function assignExpert(row) {
  currentResult.value = row
  selectedExperts.value = []
  assignDialog.value = true
}

async function confirmAssign() {
  if (!currentResult.value) {
    ElMessage.warning('未选择成果')
    return
  }
  if (!selectedExperts.value.length) {
    ElMessage.warning('请选择专家')
    return
  }

  assignLoading.value = true
  try {
    // 获取选中专家的姓名
    const selectedExpertNames = selectedExperts.value.map(id => {
      const expert = experts.value.find(e => e.id === id)
      return expert ? expert.name : `专家${id}`
    })

    await assignReviewers(currentResult.value.id, {
      reviewerIds: selectedExperts.value,
      reviewerNames: selectedExpertNames
    })

    ElMessage.success('分配成功')
    assignDialog.value = false
    loadData() // 重新加载数据
  } catch (error) {
    console.error('分配失败:', error)
    ElMessage.error('分配失败')
  } finally {
    assignLoading.value = false
  }
}
</script>
