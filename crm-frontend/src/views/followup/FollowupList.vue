<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="客户/内容/负责人" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <template #header><span>跟进列表</span></template>
      <el-timeline>
        <el-timeline-item v-for="item in rows" :key="item.id" :timestamp="item.followTime || item.time" :type="item.timelineType || item.type">
          <div class="follow-card">
            <strong>{{ item.customerName || item.customer }} · {{ item.title }}</strong>
            <p>{{ item.content }}</p>
            <div class="follow-meta">
              <el-tag size="small" effect="plain">{{ item.ownerName || item.owner }}</el-tag>
              <el-tag size="small" :type="item.nextAction ? 'warning' : 'info'">{{ item.nextAction || '暂无下一步' }}</el-tag>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getFollowupPage } from '@/api/followup'

const query = reactive({ keyword: '' })
const rows = ref([
  { id: 1, customer: '星河科技有限公司', title: '报价沟通', content: '客户认可方案，需补充实施周期和付款节点。', owner: '张三', time: '今天 10:20', nextAction: '明天发送报价', type: 'primary' },
  { id: 2, customer: '云启贸易', title: '续费提醒', content: '客户希望先确认本季度使用数据。', owner: '李四', time: '昨天 16:30', nextAction: '准备使用报告', type: 'warning' },
  { id: 3, customer: '远山教育', title: '满意度回访', content: '服务反馈良好，推荐试用营销自动化插件。', owner: '王五', time: '本周一 09:40', nextAction: '', type: 'success' },
])

async function loadData() {
  try {
    const res = await getFollowupPage({ current: 1, size: 50, keyword: query.keyword })
    rows.value = res.data?.records || rows.value
  } catch {
    // 保留本地样例数据
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.follow-card {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}
.follow-card p {
  margin: 6px 0 10px;
  color: #64748b;
}
.follow-meta {
  display: flex;
  gap: 8px;
}
</style>
