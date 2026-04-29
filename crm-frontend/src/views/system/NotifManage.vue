<template>
  <div class="page-container">
    <!-- 发送通知 -->
    <el-card shadow="never" class="send-card">
      <template #header><span>发送系统广播通知</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="form.type" style="width: 200px">
            <el-option label="信息" value="info" />
            <el-option label="成功" value="success" />
            <el-option label="警告" value="warning" />
            <el-option label="错误" value="error" />
          </el-select>
        </el-form-item>
        <el-form-item label="推送渠道">
          <el-checkbox-group v-model="form.channels">
            <el-checkbox-button label="email">邮件</el-checkbox-button>
            <el-checkbox-button label="dingtalk">钉钉</el-checkbox-button>
            <el-checkbox-button label="feishu">飞书</el-checkbox-button>
            <el-checkbox-button label="sms">短信</el-checkbox-button>
            <el-checkbox-button label="wechat">微信</el-checkbox-button>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="接收人">
          <el-input
            v-model="form.receiver"
            placeholder="邮箱/手机号，多个用逗号分隔；不填则使用渠道默认接收人"
          />
        </el-form-item>
        <el-form-item label="通知内容" prop="message">
          <el-input v-model="form.message" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSend" :loading="sending" icon="Promotion" v-if="userStore.hasPermission('system:notif:send')">立即发送</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>渠道配置</span>
          <el-button size="small" icon="Refresh" @click="fetchChannels">刷新</el-button>
        </div>
      </template>
      <el-table :data="channelRows" v-loading="channelLoading" border>
        <el-table-column label="渠道" width="110">
          <template #default="{ row }">
            <el-tag :type="channelTag(row.channel)" size="small">{{ row.channelName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="saveChannel(row)" />
          </template>
        </el-table-column>
        <el-table-column label="Webhook/API" min-width="240">
          <template #default="{ row }">
            <el-input v-model="row.webhookUrl" placeholder="钉钉/飞书/短信/微信 API 地址" clearable />
          </template>
        </el-table-column>
        <el-table-column label="SMTP/发件人" min-width="220">
          <template #default="{ row }">
            <div class="inline-fields" v-if="row.channel === 'email'">
              <el-input v-model="row.smtpHost" placeholder="SMTP Host" clearable />
              <el-input v-model="row.senderAddress" placeholder="发件人" clearable />
            </div>
            <span v-else class="muted">Webhook 渠道无需配置</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" icon="Setting" @click="editChannel(row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 历史记录 -->
    <el-card shadow="never">
      <template #header><span>通知历史</span></template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="message" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="noticeType(row.type)" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sender" label="发送者" width="120" />
        <el-table-column label="渠道" width="180">
          <template #default="{ row }">
            <span>{{ channelNames(row.channels) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="pushSummary" label="推送结果" width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发送时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" icon="Tickets" @click="openLogs(row)">日志</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchHistory" @current-change="fetchHistory"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="logVisible" title="推送日志" width="760px">
      <el-table :data="pushLogs" v-loading="logLoading" border>
        <el-table-column prop="channelName" label="渠道" width="100" />
        <el-table-column prop="target" label="目标" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="logStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="response" label="响应" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="configVisible" :title="`${configForm.channelName || ''}渠道配置`" width="720px">
      <el-form :model="configForm" label-width="110px">
        <el-form-item label="启用">
          <el-switch v-model="configForm.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="Webhook/API" v-if="configForm.channel !== 'email'">
          <el-input v-model="configForm.webhookUrl" placeholder="机器人 Webhook 或短信 API 地址" />
        </el-form-item>
        <el-form-item label="密钥">
          <el-input v-model="configForm.secret" placeholder="签名密钥/API Key，可选" show-password />
        </el-form-item>
        <template v-if="configForm.channel === 'email'">
          <el-form-item label="SMTP Host">
            <el-input v-model="configForm.smtpHost" placeholder="smtp.example.com" />
          </el-form-item>
          <el-form-item label="SMTP Port">
            <el-input-number v-model="configForm.smtpPort" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item label="SMTP 账号">
            <el-input v-model="configForm.smtpUsername" placeholder="SMTP 登录账号" />
          </el-form-item>
          <el-form-item label="SMTP 密码">
            <el-input v-model="configForm.smtpPassword" placeholder="SMTP 登录密码/授权码" show-password />
          </el-form-item>
          <el-form-item label="发件人">
            <el-input v-model="configForm.senderAddress" placeholder="notice@example.com" />
          </el-form-item>
        </template>
        <el-form-item label="默认接收人">
          <el-input v-model="configForm.receiver" placeholder="邮箱/手机号，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="消息模板">
          <el-input v-model="configForm.template" type="textarea" :rows="3" placeholder="支持 ${title}/${message}/${type}" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="configForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" :loading="configSaving" @click="saveConfigForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getNotificationChannels,
  getNotificationPage,
  getNotificationPushLogs,
  sendNotification,
  updateNotificationChannel,
} from '@/api/system'

const userStore = useUserStore()

const formRef = ref(null)
const sending = ref(false)
const loading = ref(false)
const channelLoading = ref(false)
const logLoading = ref(false)
const logVisible = ref(false)
const configVisible = ref(false)
const configSaving = ref(false)
const tableData = ref([])
const channelRows = ref([])
const pushLogs = ref([])
const configForm = reactive({})

const form = reactive({ title: '', message: '', type: 'info', receiver: '', channels: [] })
const rules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  message: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
}

const pagination = reactive({ current: 1, size: 10, total: 0 })

async function handleSend() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  sending.value = true
  try {
    await sendNotification(form)
    ElMessage.success('通知已发送')
    resetForm()
    fetchHistory()
  } finally { sending.value = false }
}

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getNotificationPage({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

async function fetchChannels() {
  channelLoading.value = true
  try {
    const res = await getNotificationChannels()
    channelRows.value = res.data || []
  } finally { channelLoading.value = false }
}

async function saveChannel(row) {
  await updateNotificationChannel(row.id, row)
  ElMessage.success(`${row.channelName}配置已保存`)
}

function editChannel(row) {
  Object.assign(configForm, row)
  configVisible.value = true
}

async function saveConfigForm() {
  configSaving.value = true
  try {
    await updateNotificationChannel(configForm.id, configForm)
    ElMessage.success(`${configForm.channelName}配置已保存`)
    configVisible.value = false
    fetchChannels()
  } finally { configSaving.value = false }
}

async function openLogs(row) {
  logVisible.value = true
  logLoading.value = true
  try {
    const res = await getNotificationPushLogs(row.id)
    pushLogs.value = res.data || []
  } finally { logLoading.value = false }
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { title: '', message: '', type: 'info', receiver: '', channels: [] })
}

const channelNameMap = {
  email: '邮件',
  dingtalk: '钉钉',
  feishu: '飞书',
  sms: '短信',
  wechat: '微信',
}

function channelNames(value) {
  if (!value) return '站内'
  return value.split(',').filter(Boolean).map(item => channelNameMap[item] || item).join('、')
}

function channelTag(channel) {
  const map = { email: 'primary', dingtalk: 'success', feishu: 'warning', sms: 'info', wechat: 'success' }
  return map[channel] || 'info'
}

function logStatusType(status) {
  const map = { SUCCESS: 'success', SKIPPED: 'warning', FAILED: 'danger' }
  return map[status] || 'info'
}

function noticeType(type) {
  const map = { info: 'info', success: 'success', warning: 'warning', error: 'danger' }
  return map[type] || 'info'
}

onMounted(() => {
  fetchHistory()
  fetchChannels()
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.send-card { border-radius: 4px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.inline-fields { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.muted { color: #909399; font-size: 13px; }
</style>
