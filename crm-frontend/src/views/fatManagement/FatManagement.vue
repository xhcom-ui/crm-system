<template>
  <div class="page-container ops-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-head">
        <div>
          <h2>客户信息管理</h2>
          <p>客户全量信息模块化管理，覆盖脂肪档案、关系图谱、联系记录、住址、身份信息、网站博客等。</p>
        </div>
        <el-button type="primary" :icon="'Plus'" @click="openFullCreate">新建客户信息</el-button>
      </div>
      <div class="metric-grid">
        <div v-for="item in metrics" :key="item.label" class="metric-tile">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="客户总览" name="overview">
          <div class="module-grid">
            <div v-for="item in moduleCards" :key="item.name" class="module-card" @click="jumpModule(item.name)">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.hint }}</small>
            </div>
          </div>
          <div class="card-grid section-block">
            <div v-for="item in customerRows" :key="item.id || item.name" class="info-card">
              <div class="info-card-head">
                <strong>{{ item.name || '未命名客户' }}</strong>
                <el-tag size="small" :type="levelType(item.level)">L{{ item.level || 1 }}</el-tag>
              </div>
              <div class="info-fields">
                <span>手机号：{{ item.phone || '-' }}</span>
                <span>邮箱：{{ item.email || '-' }}</span>
                <span>公司：{{ item.company || '-' }}</span>
                <span>职位：{{ item.position || '-' }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="脂肪档案" name="fat">
          <div class="toolbar">
            <el-input v-model="query.keyword" :prefix-icon="'Search'" placeholder="客户/方案/阶段" clearable @keyup.enter="loadFatRecords" />
            <el-select v-model="query.status" clearable placeholder="状态">
              <el-option label="执行中" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="暂停" :value="0" />
            </el-select>
            <el-button type="primary" :icon="'Refresh'" @click="loadFatRecords">刷新</el-button>
            <el-button :icon="'Plus'" @click="openActiveAdd">新增</el-button>
          </div>
          <div v-loading="loading" class="card-grid">
            <div v-for="item in fatRows" :key="item.id" class="info-card">
              <div class="info-card-head">
                <strong>{{ item.customerName }}</strong>
                <el-tag :type="riskType(item.riskLevel)" size="small">{{ item.riskLevel || '低' }}</el-tag>
              </div>
              <div class="metric-line">
                <span><b>{{ item.weightKg || 0 }}</b>kg 当前</span>
                <span><b>{{ item.bodyFatRate || 0 }}</b>% 体脂</span>
                <span><b>{{ item.targetWeightKg || 0 }}</b>kg 目标</span>
              </div>
              <div class="info-fields">
                <span>阶段：{{ item.stage || '-' }}</span>
                <span>方案：{{ item.planName || '-' }}</span>
                <span>复测：{{ item.reviewDate || '-' }}</span>
              </div>
              <div class="card-actions"><el-button size="small" type="primary" @click="openFatEdit(item)">编辑</el-button></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="关系图谱" name="relationship">
          <div class="graph-toolbar">
            <div class="graph-selection">
              <span>当前关系</span>
              <strong>{{ selectedRelationshipLabel }}</strong>
            </div>
            <div class="graph-actions">
              <el-button type="primary" :icon="'Plus'" @click="openRelationshipAdd">新增</el-button>
              <el-button :disabled="!selectedRelationship" :icon="'Edit'" @click="editSelectedRelationship">编辑</el-button>
              <el-button type="danger" plain :disabled="!selectedRelationship" :icon="'Delete'" @click="deleteSelectedRelationship">删除</el-button>
              <el-button :icon="'Refresh'" @click="loadOverview">刷新</el-button>
            </div>
          </div>
          <div class="graph-board">
            <div class="graph-canvas">
              <v-chart :option="relationshipGraphOption" class="relationship-chart" autoresize @click="handleGraphClick" />
            </div>
            <div class="graph-side">
              <div class="graph-side-head">
                <strong>关系明细</strong>
                <span>{{ relationshipRows.length }} 条</span>
              </div>
              <div
                v-for="item in relationshipRows"
                :key="`${item.id}-${item.target}`"
                class="graph-row"
                :class="{ active: isRelationshipSelected(item) }"
                @click="selectRelationship(item)"
              >
                <span class="graph-node">{{ item.node || item.name }}</span>
                <span class="graph-line">{{ item.relation || '关联' }}</span>
                <span class="graph-node company">{{ item.target || '未关联' }}</span>
                <el-button size="small" type="primary" link @click.stop="openModuleEdit(item)">编辑</el-button>
                <el-button size="small" type="danger" link @click.stop="deleteRelationship(item)">删除</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="联系记录" name="contacts">
          <div class="tab-actions"><el-button :icon="'Plus'" @click="openActiveAdd">新增联系记录</el-button></div>
          <div class="card-grid">
            <div v-for="item in contactRows" :key="item.id || `${item.title}-${item.time}`" class="info-card">
              <div class="info-card-head"><strong>{{ item.title || '联系记录' }}</strong><el-tag size="small">{{ item.type || '其他' }}</el-tag></div>
              <p class="card-desc">{{ item.description || '-' }}</p>
              <div class="info-fields"><span>客户：{{ item.name || item.customerName || '-' }}</span><span>时间：{{ item.time || '-' }}</span></div>
              <div class="card-actions"><el-button size="small" type="primary" @click="openModuleEdit(item)">编辑</el-button></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="住址信息" name="address">
          <div class="tab-actions"><el-button :icon="'Plus'" @click="openActiveAdd">新增住址信息</el-button></div>
          <div class="card-grid">
            <div v-for="item in addressRows" :key="item.id || `${item.name}-${item.address}`" class="info-card">
              <div class="info-card-head"><strong>{{ item.name || item.customerName || '未命名客户' }}</strong><el-tag size="small" type="info">住址</el-tag></div>
              <p class="card-desc">{{ [item.province, item.city, item.district, item.address].filter(Boolean).join(' ') || '-' }}</p>
              <div class="info-fields"><span>电话：{{ item.phone || '-' }}</span></div>
              <div class="card-actions"><el-button size="small" type="primary" @click="openModuleEdit(item)">编辑</el-button></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="身份信息" name="identity">
          <div class="tab-actions"><el-button :icon="'Plus'" @click="openActiveAdd">新增身份信息</el-button></div>
          <div class="card-grid">
            <div v-for="item in identityRows" :key="item.id || `${item.name}-${item.identityNo}`" class="info-card">
              <div class="info-card-head"><strong>{{ item.name || item.customerName || '未命名客户' }}</strong><el-tag size="small">{{ item.identityType || '证件' }}</el-tag></div>
              <div class="info-fields">
                <span>证件号：{{ item.identityNo || '-' }}</span>
                <span>生日：{{ item.birthday || '-' }}</span>
                <span>性别：{{ item.gender || '-' }}</span>
                <span>邮箱：{{ item.email || '-' }}</span>
              </div>
              <div class="card-actions"><el-button size="small" type="primary" @click="openModuleEdit(item)">编辑</el-button></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="网站博客" name="website">
          <div class="tab-actions"><el-button :icon="'Plus'" @click="openActiveAdd">新增网站博客</el-button></div>
          <div class="card-grid">
            <div v-for="item in websiteRows" :key="item.id || `${item.name}-${item.website}`" class="info-card">
              <div class="info-card-head"><strong>{{ item.name || item.customerName || '未命名客户' }}</strong><el-tag size="small" type="success">内容</el-tag></div>
              <div class="info-fields">
                <span>网站：{{ item.website || '-' }}</span>
                <span>博客：{{ item.blog || '-' }}</span>
                <span>偏好：{{ item.contentPreference || '-' }}</span>
              </div>
              <div class="card-actions"><el-button size="small" type="primary" @click="openModuleEdit(item)">编辑</el-button></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="阶段分布" name="stages">
          <div class="stage-grid">
            <div v-for="item in stages" :key="item.name" class="stage-card">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="待补能力" name="missing">
          <div class="capability-actions">
            <el-button v-for="item in missingCapabilities" :key="item" type="warning" plain @click="openCapability(item)">
              {{ item }}
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="fatDialogTitle" width="680px" @close="resetForm">
      <el-form :model="form" label-width="110px" class="dialog-grid">
        <el-form-item label="客户姓名"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="form.age" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="身高 cm"><el-input-number v-model="form.heightCm" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="体重 kg"><el-input-number v-model="form.weightKg" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="体脂率"><el-input-number v-model="form.bodyFatRate" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="目标体重"><el-input-number v-model="form.targetWeightKg" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="阶段"><el-input v-model="form.stage" /></el-form-item>
        <el-form-item label="方案"><el-input v-model="form.planName" /></el-form-item>
        <el-form-item label="风险"><el-select v-model="form.riskLevel" style="width: 100%"><el-option label="低" value="低" /><el-option label="中" value="中" /><el-option label="高" value="高" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="capabilityDialog" :title="selectedCapability" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="能力说明">{{ capabilityDesc(selectedCapability) }}</el-descriptions-item>
        <el-descriptions-item label="建议模块">客户信息管理 / {{ selectedCapability }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">待建设</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button type="primary" @click="capabilityDialog = false">知道了</el-button></template>
    </el-dialog>

    <el-dialog v-model="moduleDialogVisible" :title="moduleDialogTitle" width="680px" @close="resetModuleForm">
      <el-form :model="moduleForm" label-width="100px" class="dialog-grid">
        <el-form-item label="客户姓名"><el-input v-model="moduleForm.customerName" /></el-form-item>
        <template v-if="moduleType === 'identity'">
          <el-form-item label="证件类型"><el-select v-model="moduleForm.identityType" style="width: 100%"><el-option label="身份证" value="身份证" /><el-option label="护照" value="护照" /><el-option label="其他" value="其他" /></el-select></el-form-item>
          <el-form-item label="证件号"><el-input v-model="moduleForm.identityNo" /></el-form-item>
          <el-form-item label="生日"><el-date-picker v-model="moduleForm.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
          <el-form-item label="性别"><el-select v-model="moduleForm.gender" style="width: 100%"><el-option label="男" value="男" /><el-option label="女" value="女" /><el-option label="未知" value="未知" /></el-select></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="moduleForm.email" /></el-form-item>
        </template>
        <template v-else-if="moduleType === 'address'">
          <el-form-item label="省"><el-input v-model="moduleForm.province" /></el-form-item>
          <el-form-item label="市"><el-input v-model="moduleForm.city" /></el-form-item>
          <el-form-item label="区县"><el-input v-model="moduleForm.district" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="moduleForm.phone" /></el-form-item>
          <el-form-item label="详细住址" class="form-wide"><el-input v-model="moduleForm.address" /></el-form-item>
        </template>
        <template v-else-if="moduleType === 'relationship'">
          <el-form-item label="关系"><el-input v-model="moduleForm.relation" placeholder="任职/同事/亲属/合作" /></el-form-item>
          <el-form-item label="对象"><el-input v-model="moduleForm.target" placeholder="公司/联系人/组织" /></el-form-item>
        </template>
        <template v-else-if="moduleType === 'contact'">
          <el-form-item label="类型"><el-select v-model="moduleForm.contactType" style="width: 100%"><el-option label="电话" value="电话" /><el-option label="邮件" value="邮件" /><el-option label="会议" value="会议" /><el-option label="拜访" value="拜访" /></el-select></el-form-item>
          <el-form-item label="标题"><el-input v-model="moduleForm.title" /></el-form-item>
          <el-form-item label="时间"><el-date-picker v-model="moduleForm.eventTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
          <el-form-item label="内容" class="form-wide"><el-input v-model="moduleForm.description" type="textarea" :rows="3" /></el-form-item>
        </template>
        <template v-else-if="moduleType === 'website'">
          <el-form-item label="网站"><el-input v-model="moduleForm.website" /></el-form-item>
          <el-form-item label="博客"><el-input v-model="moduleForm.blog" /></el-form-item>
          <el-form-item label="内容偏好" class="form-wide"><el-input v-model="moduleForm.contentPreference" type="textarea" :rows="3" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="moduleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="moduleSaving" @click="saveModuleItem">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="fullDialogVisible" title="新建客户信息" width="920px" @close="resetFullForm">
      <el-form :model="fullForm" label-width="100px">
        <div class="form-section">
          <h3>客户主档</h3>
          <div class="dialog-grid">
            <el-form-item label="客户姓名" required><el-input v-model="fullForm.name" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="fullForm.phone" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="fullForm.email" /></el-form-item>
            <el-form-item label="公司"><el-input v-model="fullForm.company" /></el-form-item>
            <el-form-item label="职位"><el-input v-model="fullForm.position" /></el-form-item>
            <el-form-item label="客户等级"><el-select v-model="fullForm.level" style="width: 100%"><el-option label="普通" :value="1" /><el-option label="重要" :value="2" /><el-option label="VIP" :value="3" /></el-select></el-form-item>
          </div>
        </div>
        <div class="form-section">
          <h3>脂肪档案</h3>
          <div class="dialog-grid">
            <el-form-item label="年龄"><el-input-number v-model="fullForm.age" :min="1" style="width: 100%" /></el-form-item>
            <el-form-item label="身高 cm"><el-input-number v-model="fullForm.heightCm" :min="0" style="width: 100%" /></el-form-item>
            <el-form-item label="体重 kg"><el-input-number v-model="fullForm.weightKg" :min="0" style="width: 100%" /></el-form-item>
            <el-form-item label="体脂率"><el-input-number v-model="fullForm.bodyFatRate" :min="0" style="width: 100%" /></el-form-item>
            <el-form-item label="目标体重"><el-input-number v-model="fullForm.targetWeightKg" :min="0" style="width: 100%" /></el-form-item>
            <el-form-item label="阶段"><el-input v-model="fullForm.stage" /></el-form-item>
            <el-form-item label="方案"><el-input v-model="fullForm.planName" /></el-form-item>
            <el-form-item label="风险"><el-select v-model="fullForm.riskLevel" style="width: 100%"><el-option label="低" value="低" /><el-option label="中" value="中" /><el-option label="高" value="高" /></el-select></el-form-item>
          </div>
        </div>
        <div class="form-section">
          <h3>身份与住址</h3>
          <div class="dialog-grid">
            <el-form-item label="证件类型"><el-select v-model="fullForm.identityType" style="width: 100%"><el-option label="身份证" value="身份证" /><el-option label="护照" value="护照" /><el-option label="其他" value="其他" /></el-select></el-form-item>
            <el-form-item label="证件号"><el-input v-model="fullForm.identityNo" /></el-form-item>
            <el-form-item label="生日"><el-date-picker v-model="fullForm.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
            <el-form-item label="性别"><el-select v-model="fullForm.gender" style="width: 100%"><el-option label="男" value="男" /><el-option label="女" value="女" /><el-option label="未知" value="未知" /></el-select></el-form-item>
            <el-form-item label="省"><el-input v-model="fullForm.province" /></el-form-item>
            <el-form-item label="市"><el-input v-model="fullForm.city" /></el-form-item>
            <el-form-item label="区县"><el-input v-model="fullForm.district" /></el-form-item>
            <el-form-item label="详细住址"><el-input v-model="fullForm.address" /></el-form-item>
          </div>
        </div>
        <div class="form-section">
          <h3>关系、联系与内容</h3>
          <div class="dialog-grid">
            <el-form-item label="关系"><el-input v-model="fullForm.relation" placeholder="任职/同事/亲属/合作" /></el-form-item>
            <el-form-item label="关系对象"><el-input v-model="fullForm.target" /></el-form-item>
            <el-form-item label="联系类型"><el-select v-model="fullForm.contactType" style="width: 100%"><el-option label="电话" value="电话" /><el-option label="邮件" value="邮件" /><el-option label="会议" value="会议" /><el-option label="拜访" value="拜访" /></el-select></el-form-item>
            <el-form-item label="联系标题"><el-input v-model="fullForm.contactTitle" /></el-form-item>
            <el-form-item label="网站"><el-input v-model="fullForm.website" /></el-form-item>
            <el-form-item label="博客"><el-input v-model="fullForm.blog" /></el-form-item>
            <el-form-item label="内容偏好" class="form-wide"><el-input v-model="fullForm.contentPreference" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="联系内容" class="form-wide"><el-input v-model="fullForm.description" type="textarea" :rows="2" /></el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="fullDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="fullSaving" @click="saveFullCustomer">保存全部</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GraphChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent } from 'echarts/components'
import {
  addCustomerInfoItem,
  addFatRecord,
  deleteCustomerInfoItem,
  getFatOverview,
  getFatRecordPage,
  updateCustomerInfoItem,
  updateFatRecord,
} from '@/api/fatManagement'
import { addContact } from '@/api/customer'

use([CanvasRenderer, GraphChart, TooltipComponent, LegendComponent])

const activeTab = ref('overview')
const dialogVisible = ref(false)
const capabilityDialog = ref(false)
const moduleDialogVisible = ref(false)
const moduleSaving = ref(false)
const fullDialogVisible = ref(false)
const fullSaving = ref(false)
const fatEditingId = ref(null)
const moduleEditingId = ref(null)
const selectedRelationship = ref(null)
const selectedCapability = ref('')
const loading = ref(false)
const query = reactive({ keyword: '', status: '' })
const metrics = ref([])
const stages = ref([])
const missingCapabilities = ref([])
const moduleCards = ref([])
const customerRows = ref([])
const identityRows = ref([])
const addressRows = ref([])
const relationshipRows = ref([])
const contactRows = ref([])
const websiteRows = ref([])
const fatRows = ref([])
const form = reactive({ customerName: '', age: 30, heightCm: 170, weightKg: 70, bodyFatRate: 25, targetWeightKg: 62, stage: '建档评估', planName: '基础减脂方案', riskLevel: '中', status: 1 })
const moduleForm = reactive({})
const fullForm = reactive({})

const moduleType = computed(() => ({
  identity: 'identity',
  address: 'address',
  relationship: 'relationship',
  contacts: 'contact',
  website: 'website',
}[activeTab.value] || 'fat'))

const addButtonText = computed(() => ({
  fat: '新增脂肪档案',
  identity: '新增身份信息',
  address: '新增住址信息',
  relationship: '新增关系',
  contact: '新增联系记录',
  website: '新增网站博客',
}[moduleType.value] || '新增档案'))

const fatDialogTitle = computed(() => fatEditingId.value ? '编辑客户脂肪档案' : '新增客户脂肪档案')
const moduleDialogTitle = computed(() => {
  if (!moduleEditingId.value) return addButtonText.value
  return addButtonText.value.replace('新增', '编辑')
})

const selectedRelationshipLabel = computed(() => {
  const item = selectedRelationship.value
  if (!item) return '未选择'
  return `${item.node || item.name || item.customerName || '-'} ${item.relation || '关联'} ${item.target || '-'}`
})

const relationshipGraphOption = computed(() => {
  const rows = relationshipRows.value.length
    ? relationshipRows.value
    : [{ id: 'empty', node: '暂无客户', name: '暂无客户', relation: '待补充', target: '暂无关系' }]
  const nodeMap = new Map()
  const links = []

  rows.forEach((item, index) => {
    const source = item.node || item.name || `客户${index + 1}`
    const target = item.target || '未关联对象'
    const relation = item.relation || '关联'
    if (!nodeMap.has(source)) {
      nodeMap.set(source, {
        id: source,
        name: source,
        category: 0,
        symbolSize: 58,
        itemStyle: { color: '#409eff' },
        label: { color: '#1f2937', fontWeight: 600 },
      })
    }
    if (!nodeMap.has(target)) {
      nodeMap.set(target, {
        id: target,
        name: target,
        category: 1,
        symbolSize: 68,
        itemStyle: { color: '#67c23a' },
        label: { color: '#1f2937', fontWeight: 600 },
      })
    }
    links.push({
      source,
      target,
      value: relation,
      rowId: item.id,
      label: { show: true, formatter: relation, color: '#64748b', fontSize: 12 },
      lineStyle: { width: 2, color: '#a8b3c7', curveness: 0.18 },
    })
  })

  return {
    color: ['#409eff', '#67c23a', '#e6a23c'],
    tooltip: {
      trigger: 'item',
      formatter(params) {
        if (params.dataType === 'edge') return `${params.data.source} ${params.data.value} ${params.data.target}`
        return params.name
      },
    },
    legend: {
      top: 8,
      data: ['客户', '组织/对象'],
      textStyle: { color: '#606266' },
    },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      top: 48,
      bottom: 16,
      left: 16,
      right: 16,
      categories: [{ name: '客户' }, { name: '组织/对象' }],
      data: Array.from(nodeMap.values()),
      links,
      label: {
        show: true,
        position: 'right',
        fontSize: 13,
      },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 8],
      force: {
        repulsion: 280,
        edgeLength: [90, 150],
        gravity: 0.08,
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 4 },
      },
    }],
  }
})

async function loadOverview() {
  const res = await getFatOverview()
  const data = res.data || {}
  metrics.value = data.metrics || []
  stages.value = data.stageRows || []
  missingCapabilities.value = data.missingCapabilities || []
  moduleCards.value = data.moduleCards || []
  customerRows.value = data.customerRows || []
  identityRows.value = data.identityRows || []
  addressRows.value = data.addressRows || []
  relationshipRows.value = data.relationshipRows || []
  if (selectedRelationship.value) {
    selectedRelationship.value = relationshipRows.value.find(item => sameRelationship(item, selectedRelationship.value)) || null
  }
  contactRows.value = data.contactRows || []
  websiteRows.value = data.websiteRows || []
}

async function loadFatRecords() {
  loading.value = true
  try {
    const res = await getFatRecordPage({ current: 1, size: 50, ...query })
    fatRows.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

async function saveRecord() {
  if (fatEditingId.value) {
    await updateFatRecord(fatEditingId.value, form)
  } else {
    await addFatRecord(form)
  }
  ElMessage.success(fatEditingId.value ? '客户脂肪档案已更新' : '客户脂肪档案已保存')
  dialogVisible.value = false
  await Promise.all([loadOverview(), loadFatRecords()])
}

function riskType(value) {
  return value === '高' ? 'danger' : value === '中' ? 'warning' : 'success'
}

function levelType(value) {
  return value === 3 ? 'danger' : value === 2 ? 'warning' : 'info'
}

function jumpModule(name) {
  const map = {
    脂肪档案: 'fat',
    身份信息: 'identity',
    住址信息: 'address',
    联系记录: 'contacts',
    关系图谱: 'relationship',
    网站博客: 'website',
  }
  activeTab.value = map[name] || 'overview'
}

function openCapability(item) {
  selectedCapability.value = item
  capabilityDialog.value = true
}

function openFullCreate() {
  resetFullForm()
  fullDialogVisible.value = true
}

function openActiveAdd() {
  if (moduleType.value === 'fat') {
    resetForm()
    dialogVisible.value = true
    return
  }
  moduleEditingId.value = null
  resetModuleForm()
  if (moduleType.value === 'identity') Object.assign(moduleForm, { identityType: '身份证', gender: '未知' })
  if (moduleType.value === 'contact') Object.assign(moduleForm, { contactType: '电话' })
  moduleDialogVisible.value = true
}

function openRelationshipAdd() {
  activeTab.value = 'relationship'
  openActiveAdd()
}

function openFatEdit(row) {
  fatEditingId.value = row.id
  Object.assign(form, {
    customerName: row.customerName || '',
    age: row.age ?? 30,
    heightCm: row.heightCm ?? 170,
    weightKg: row.weightKg ?? 70,
    bodyFatRate: row.bodyFatRate ?? 25,
    targetWeightKg: row.targetWeightKg ?? 62,
    stage: row.stage || '建档评估',
    planName: row.planName || '基础减脂方案',
    riskLevel: row.riskLevel || '中',
    status: row.status ?? 1,
  })
  dialogVisible.value = true
}

function openModuleEdit(row) {
  if (moduleType.value === 'relationship') selectRelationship(row)
  moduleEditingId.value = row.moduleItem ? row.id : null
  resetModuleForm()
  Object.assign(moduleForm, {
    customerName: row.customerName || row.name || row.node || '',
    identityType: row.identityType || '身份证',
    identityNo: row.identityNo === '待补充' ? '' : (row.identityNo || ''),
    birthday: row.birthday === '待补充' ? '' : (row.birthday || ''),
    gender: row.gender === '待补充' ? '未知' : (row.gender || '未知'),
    email: row.email || '',
    phone: row.phone || '',
    province: row.province === '待补充' ? '' : (row.province || ''),
    city: row.city === '待补充' ? '' : (row.city || ''),
    district: row.district === '待补充' ? '' : (row.district || ''),
    address: row.address === '待补充详细住址' ? '' : (row.address || ''),
    relation: row.relation || '',
    target: row.target === '未知企业' ? '' : (row.target || ''),
    contactType: row.type || '电话',
    title: row.title || '',
    description: row.description || '',
    eventTime: row.time || '',
    website: row.website || '',
    blog: row.blog || '',
    contentPreference: row.contentPreference || '',
  })
  moduleDialogVisible.value = true
}

function selectRelationship(item) {
  selectedRelationship.value = item
}

function isRelationshipSelected(item) {
  return selectedRelationship.value && sameRelationship(item, selectedRelationship.value)
}

function sameRelationship(a, b) {
  if (!a || !b) return false
  if (a.moduleItem && b.moduleItem) return a.id === b.id
  return (a.node || a.name || a.customerName) === (b.node || b.name || b.customerName)
    && a.relation === b.relation
    && a.target === b.target
}

function handleGraphClick(params) {
  if (!params) return
  let row = null
  if (params.dataType === 'edge') {
    row = relationshipRows.value.find(item => {
      const source = item.node || item.name || item.customerName
      const target = item.target || '未关联对象'
      return source === params.data.source && target === params.data.target && (item.relation || '关联') === params.data.value
    })
  } else {
    row = relationshipRows.value.find(item => {
      const source = item.node || item.name || item.customerName
      return source === params.name || item.target === params.name
    })
  }
  if (row) selectRelationship(row)
}

function editSelectedRelationship() {
  if (!selectedRelationship.value) return
  openModuleEdit(selectedRelationship.value)
}

async function deleteSelectedRelationship() {
  if (!selectedRelationship.value) return
  await deleteRelationship(selectedRelationship.value)
}

async function deleteRelationship(item) {
  if (!item?.moduleItem) {
    ElMessage.warning('系统默认关系请先编辑保存后再删除')
    return
  }
  try {
    await ElMessageBox.confirm('确认删除这条关系吗？', '删除关系', { type: 'warning' })
  } catch {
    return
  }
  await deleteCustomerInfoItem('relationship', item.id)
  ElMessage.success('关系已删除')
  if (isRelationshipSelected(item)) selectedRelationship.value = null
  await loadOverview()
}

async function saveModuleItem() {
  if (!moduleForm.customerName) {
    ElMessage.warning('请输入客户姓名')
    return
  }
  moduleSaving.value = true
  try {
    if (moduleEditingId.value) {
      await updateCustomerInfoItem(moduleType.value, moduleEditingId.value, moduleForm)
    } else {
      await addCustomerInfoItem(moduleType.value, moduleForm)
    }
    ElMessage.success(moduleEditingId.value ? '已更新' : '已保存')
    moduleDialogVisible.value = false
    await loadOverview()
  } finally {
    moduleSaving.value = false
  }
}

async function saveFullCustomer() {
  if (!fullForm.name) {
    ElMessage.warning('请输入客户姓名')
    return
  }
  fullSaving.value = true
  try {
    const customerName = fullForm.name
    await addContact({
      name: customerName,
      phone: fullForm.phone,
      email: fullForm.email,
      company: fullForm.company,
      position: fullForm.position,
      level: fullForm.level || 1,
      source: 4,
      remark: fullForm.contentPreference,
    })
    await addFatRecord({
      customerName,
      age: fullForm.age,
      heightCm: fullForm.heightCm,
      weightKg: fullForm.weightKg,
      bodyFatRate: fullForm.bodyFatRate,
      targetWeightKg: fullForm.targetWeightKg,
      stage: fullForm.stage,
      planName: fullForm.planName,
      riskLevel: fullForm.riskLevel,
      status: 1,
    })
    await Promise.all([
      addCustomerInfoItem('identity', {
        customerName,
        identityType: fullForm.identityType,
        identityNo: fullForm.identityNo,
        birthday: fullForm.birthday,
        gender: fullForm.gender,
        email: fullForm.email,
        phone: fullForm.phone,
      }),
      addCustomerInfoItem('address', {
        customerName,
        province: fullForm.province,
        city: fullForm.city,
        district: fullForm.district,
        address: fullForm.address,
        phone: fullForm.phone,
      }),
      addCustomerInfoItem('relationship', {
        customerName,
        relation: fullForm.relation,
        target: fullForm.target || fullForm.company,
      }),
      addCustomerInfoItem('contact', {
        customerName,
        contactType: fullForm.contactType,
        title: fullForm.contactTitle,
        description: fullForm.description,
        eventTime: '',
      }),
      addCustomerInfoItem('website', {
        customerName,
        website: fullForm.website,
        blog: fullForm.blog,
        contentPreference: fullForm.contentPreference,
      }),
    ])
    ElMessage.success('客户相关信息已创建')
    fullDialogVisible.value = false
    await Promise.all([loadOverview(), loadFatRecords()])
  } finally {
    fullSaving.value = false
  }
}

function capabilityDesc(name) {
  return {
    统一客户ID合并: '跨手机号、邮箱、设备号、平台账号合并为统一客户主档。',
    证件OCR识别: '自动识别身份证、护照等证件图片并回填身份字段。',
    地址标准化: '将自由文本住址标准化为省、市、区县、街道和经纬度。',
    社交图谱自动发现: '从互动、企业、同事关系中自动生成客户关系图谱。',
    博客舆情监测: '监控客户网站、博客、社媒内容变化和风险关键词。',
    隐私授权管理: '记录客户授权范围、有效期、撤回记录和敏感字段访问审计。',
  }[name] || '客户信息模块扩展能力。'
}

function resetForm() {
  fatEditingId.value = null
  Object.assign(form, { customerName: '', age: 30, heightCm: 170, weightKg: 70, bodyFatRate: 25, targetWeightKg: 62, stage: '建档评估', planName: '基础减脂方案', riskLevel: '中', status: 1 })
}

function resetModuleForm() {
  moduleEditingId.value = null
  Object.keys(moduleForm).forEach(key => delete moduleForm[key])
  Object.assign(moduleForm, { customerName: '' })
}

function resetFullForm() {
  Object.keys(fullForm).forEach(key => delete fullForm[key])
  Object.assign(fullForm, {
    name: '',
    phone: '',
    email: '',
    company: '',
    position: '',
    level: 1,
    age: 30,
    heightCm: 170,
    weightKg: 70,
    bodyFatRate: 25,
    targetWeightKg: 62,
    stage: '建档评估',
    planName: '基础减脂方案',
    riskLevel: '中',
    identityType: '身份证',
    identityNo: '',
    birthday: '',
    gender: '未知',
    province: '',
    city: '',
    district: '',
    address: '',
    relation: '任职',
    target: '',
    contactType: '电话',
    contactTitle: '首次建档沟通',
    description: '',
    website: '',
    blog: '',
    contentPreference: '',
  })
}

onMounted(() => Promise.all([loadOverview(), loadFatRecords()]))
</script>

<style scoped>
.ops-page { display: flex; flex-direction: column; gap: 16px; }
.hero-head, .toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.hero-head h2 { margin: 0 0 6px; font-size: 22px; }
.hero-head p { margin: 0; color: var(--el-text-color-secondary); }
.metric-grid, .stage-grid, .module-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-top: 16px; }
.metric-tile, .stage-card, .module-card { border: 1px solid var(--el-border-color-lighter); border-radius: 8px; padding: 14px; background: #fff; }
.module-card { cursor: pointer; transition: border-color .2s, box-shadow .2s; }
.module-card:hover { border-color: #409eff; box-shadow: 0 2px 10px rgba(64, 158, 255, 0.12); }
.metric-tile span, .stage-card span, .module-card span { display: block; color: var(--el-text-color-secondary); font-size: 13px; }
.metric-tile strong, .stage-card strong, .module-card strong { display: block; margin-top: 8px; font-size: 22px; }
.metric-tile small, .module-card small { color: var(--el-text-color-placeholder); }
.toolbar .el-input { max-width: 260px; }
.section-table { margin-top: 14px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 12px; }
.form-wide { grid-column: 1 / -1; }
.section-block { margin-top: 14px; }
.tab-actions { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.card-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.info-card { min-height: 150px; border: 1px solid var(--el-border-color-lighter); border-radius: 8px; padding: 14px; background: #fff; display: flex; flex-direction: column; gap: 12px; }
.info-card-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.info-card-head strong { font-size: 16px; color: var(--el-text-color-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.info-fields { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px 12px; color: var(--el-text-color-secondary); font-size: 13px; line-height: 1.5; }
.info-fields span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.metric-line { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; color: var(--el-text-color-secondary); font-size: 13px; }
.metric-line b { display: block; color: var(--el-text-color-primary); font-size: 18px; }
.card-desc { margin: 0; min-height: 42px; color: var(--el-text-color-secondary); line-height: 1.6; }
.card-actions { margin-top: auto; display: flex; justify-content: flex-end; }
.form-section { border: 1px solid var(--el-border-color-lighter); border-radius: 8px; padding: 14px 14px 0; margin-bottom: 12px; }
.form-section h3 { margin: 0 0 14px; font-size: 15px; color: var(--el-text-color-primary); }
.capability-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.graph-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; padding: 12px; border: 1px solid var(--el-border-color-lighter); border-radius: 8px; background: #fff; }
.graph-selection { display: flex; align-items: center; gap: 10px; color: var(--el-text-color-secondary); min-width: 0; }
.graph-selection strong { color: var(--el-text-color-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.graph-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.graph-board { display: grid; grid-template-columns: minmax(0, 1fr) 360px; gap: 12px; min-height: 420px; }
.graph-canvas, .graph-side { border: 1px solid var(--el-border-color-lighter); border-radius: 8px; background: #fff; }
.graph-canvas { min-height: 420px; overflow: hidden; }
.relationship-chart { width: 100%; height: 420px; }
.graph-side { padding: 12px; overflow: auto; max-height: 420px; }
.graph-side-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; color: var(--el-text-color-secondary); }
.graph-side-head strong { color: var(--el-text-color-primary); }
.graph-row { display: flex; align-items: center; gap: 10px; padding: 12px; border: 1px solid var(--el-border-color-lighter); border-radius: 8px; background: #fff; }
.graph-row { cursor: pointer; transition: border-color .2s, box-shadow .2s, background .2s; }
.graph-row.active { border-color: #409eff; background: #f4f9ff; box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12); }
.graph-row + .graph-row { margin-top: 10px; }
.graph-node { padding: 8px 12px; border-radius: 8px; background: #ecf5ff; color: #2563eb; font-weight: 600; }
.graph-node.company { background: #f0f9eb; color: #3f9b2f; }
.graph-line { color: var(--el-text-color-secondary); white-space: nowrap; }
@media (max-width: 1200px) { .card-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 900px) { .metric-grid, .stage-grid, .module-grid, .dialog-grid, .graph-board, .card-grid, .info-fields, .metric-line { grid-template-columns: 1fr; } .hero-head, .toolbar, .graph-toolbar { align-items: stretch; flex-direction: column; } }
</style>
