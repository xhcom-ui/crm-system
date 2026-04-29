<template>
  <div class="page-container">
    <div class="page-heading">
      <div>
        <h1>SLA 与知识库</h1>
        <p>统一查看工单响应承诺、升级策略和常见问题处理方案。</p>
      </div>
      <el-button :icon="'ArrowLeft'" @click="router.push('/service')">返回工单</el-button>
    </div>

    <el-row :gutter="16">
      <el-col v-for="item in slaCards" :key="item.title" :xs="12" :md="6">
        <div class="sla-card" :class="item.tone">
          <span>{{ item.title }}</span>
          <strong>{{ item.time }}</strong>
          <small>{{ item.desc }}</small>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>知识库</span>
              <div class="header-actions">
                <el-input v-model="keyword" placeholder="搜索问题/标签" clearable style="width: 240px" />
                <el-button type="primary" @click="dialogVisible = true">新增文章</el-button>
              </div>
            </div>
          </template>
          <el-table :data="filteredArticles" border stripe>
            <el-table-column prop="title" label="标题" min-width="220" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column label="标签" min-width="180">
              <template #default="{ row }">
                <el-tag v-for="tag in row.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="used" label="引用次数" width="100" />
            <el-table-column label="建议动作" width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="selected = row">查看方案</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="solution-card">
          <template #header><span>处理方案</span></template>
          <h3>{{ selected.title }}</h3>
          <p>{{ selected.solution }}</p>
          <el-divider />
          <div class="playbook">
            <div v-for="step in selected.steps" :key="step">
              <el-icon><Check /></el-icon>
              <span>{{ step }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="新增知识库文章" width="640px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="例如：数据服务" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="方案" prop="solution">
          <el-input v-model="form.solution" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="步骤">
          <el-input v-model="form.steps" type="textarea" :rows="3" placeholder="多个步骤用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitArticle">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { addKnowledgeArticle, getKnowledgeOverview } from '@/api/service'

const router = useRouter()
const keyword = ref('')
const formRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)

const slaCards = ref([
  { title: '紧急工单', time: '15 分钟', desc: '超时自动升级主管', tone: 'critical' },
  { title: '高优先级', time: '1 小时', desc: '需当天给出方案', tone: 'high' },
  { title: '普通咨询', time: '4 小时', desc: '工作时间内响应', tone: 'normal' },
  { title: '建议反馈', time: '1 天', desc: '进入需求池评估', tone: 'low' },
])

const articles = ref([
  {
    title: '数据导入失败排查',
    category: '数据服务',
    tags: ['导入', '模板', '字段映射'],
    used: 128,
    solution: '先确认模板版本和必填字段，再检查手机号、邮箱等唯一字段是否重复。',
    steps: ['下载最新模板', '校验必填字段', '检查重复数据', '重新发起导入任务'],
  },
  {
    title: '客户无法收到营销邮件',
    category: '营销触达',
    tags: ['邮件', '退订', '送达率'],
    used: 96,
    solution: '检查客户是否退订、邮箱是否无效，以及活动发送域名是否通过校验。',
    steps: ['确认订阅状态', '验证邮箱格式', '检查发送记录', '切换备用触达渠道'],
  },
  {
    title: '工作流节点未自动流转',
    category: '自动化',
    tags: ['工作流', '节点', '审批'],
    used: 72,
    solution: '确认工作流启用状态、节点顺序和触发条件，必要时手动重试实例。',
    steps: ['检查工作流状态', '查看实例日志', '确认节点配置', '重新触发流程'],
  },
])

const selected = ref(articles.value[0])
const form = reactive({
  title: '',
  category: '',
  tags: '',
  solution: '',
  steps: '',
  usedCount: 0,
  status: 1,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  solution: [{ required: true, message: '请输入处理方案', trigger: 'blur' }],
}

const filteredArticles = computed(() => {
  const value = keyword.value.trim()
  if (!value) return articles.value
  return articles.value.filter(item => [item.title, item.category, ...item.tags].some(text => text.includes(value)))
})

async function loadOverview() {
  try {
    const res = await getKnowledgeOverview()
    if (res.data?.slaCards?.length) slaCards.value = res.data.slaCards
    if (res.data?.articles?.length) {
      articles.value = res.data.articles
      selected.value = articles.value[0]
    }
  } catch {
    selected.value = articles.value[0]
  }
}

async function submitArticle() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await addKnowledgeArticle(form)
    ElMessage.success('知识库文章已保存')
    dialogVisible.value = false
    await loadOverview()
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    title: '',
    category: '',
    tags: '',
    solution: '',
    steps: '',
    usedCount: 0,
    status: 1,
  })
}

onMounted(() => loadOverview())
</script>

<style scoped>
.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-heading h1 {
  margin: 0;
  font-size: 24px;
}

.page-heading p,
.solution-card p {
  margin: 6px 0 0;
  color: #64748b;
}

.sla-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 118px;
  padding: 18px;
  border-radius: 8px;
  color: #fff;
}

.sla-card strong {
  font-size: 28px;
}

.sla-card small {
  color: rgba(255, 255, 255, 0.78);
}

.sla-card.critical { background: #dc2626; }
.sla-card.high { background: #f59e0b; }
.sla-card.normal { background: #2563eb; }
.sla-card.low { background: #0f766e; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.solution-card {
  height: 100%;
}

.playbook {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.playbook div {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .page-heading,
  .card-header,
  .header-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
