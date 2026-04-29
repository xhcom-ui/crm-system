<template>
  <div class="sentinel-page">
    <div class="sentinel-toolbar">
      <div>
        <h2>Sentinel 控制台</h2>
        <span>{{ dashboardUrl }}</span>
      </div>
      <div class="toolbar-actions">
        <el-button :icon="'Refresh'" @click="reloadFrame">刷新</el-button>
        <el-button type="primary" :icon="'TopRight'" @click="openExternal">新窗口打开</el-button>
      </div>
    </div>

    <div class="sentinel-frame-wrap">
      <iframe
        ref="frameRef"
        class="sentinel-frame"
        :src="frameSrc"
        title="Sentinel Dashboard"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const dashboardUrl = import.meta.env.VITE_SENTINEL_DASHBOARD_URL || 'http://127.0.0.1:8858'
const frameSrc = ref(dashboardUrl)
const frameRef = ref(null)

function reloadFrame() {
  frameSrc.value = ''
  requestAnimationFrame(() => {
    frameSrc.value = `${dashboardUrl}${dashboardUrl.includes('?') ? '&' : '?'}_t=${Date.now()}`
  })
}

function openExternal() {
  window.open(dashboardUrl, '_blank', 'noopener,noreferrer')
}
</script>

<style scoped>
.sentinel-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  min-height: 560px;
}

.sentinel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 0 0 14px;
}

.sentinel-toolbar h2 {
  margin: 0 0 4px;
  font-size: 20px;
  color: #1f2937;
}

.sentinel-toolbar span {
  color: #64748b;
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.sentinel-frame-wrap {
  flex: 1;
  min-height: 0;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.sentinel-frame {
  width: 100%;
  height: 100%;
  border: 0;
  display: block;
  background: #fff;
}

@media (max-width: 768px) {
  .sentinel-page {
    height: calc(100vh - 84px);
    min-height: 480px;
  }

  .sentinel-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
