import { ref, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'

/**
 * SSE (Server-Sent Events) 通知客户端 composable
 * 使用原生 EventSource API，自动重连
 */
export function useSSE() {
  const connected = ref(false)
  let eventSource = null
  let reconnectTimer = null
  let isManuallyClosed = false

  function connect() {
    // 通过 Vite 代理连接 SSE
    const protocol = window.location.protocol
    const token = localStorage.getItem('token') || ''

    // SSE 连接 URL - 指向任意已部署服务（通过网关）
    const url = `${protocol}//${window.location.host}/api/sse/notification/subscribe`

    try {
      // EventSource 不支持自定义请求头，通过 URL 参数传递 token
      eventSource = new EventSource(`${url}?token=${encodeURIComponent(token)}`)

      // 连接建立
      eventSource.onopen = () => {
        connected.value = true
        console.log('[SSE] 通知连接已建立')
      }

      // 接收消息（自定义 event: notification）
      eventSource.addEventListener('notification', (event) => {
        try {
          const data = JSON.parse(event.data)
          ElNotification({
            title: data.title || '系统通知',
            message: data.message || '',
            type: data.type || 'info',
            duration: 5000,
          })
          window.dispatchEvent(new CustomEvent('crm-notification', { detail: data }))
        } catch (e) {
          console.warn('[SSE] 消息解析失败:', e)
        }
      })

      // 连接成功确认事件
      eventSource.addEventListener('connected', (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('[SSE] 客户端ID:', data.clientId)
        } catch (e) { /* ignore */ }
      })

      // 默认 message 事件（兜底）
      eventSource.onmessage = (event) => {
        if (!event.data || event.data.trim() === '') return
        try {
          const data = JSON.parse(event.data)
          if (data.type !== 'connected') {
            ElNotification({
              title: data.title || '通知',
              message: data.message || event.data,
              type: data.type || 'info',
              duration: 5000,
            })
            window.dispatchEvent(new CustomEvent('crm-notification', { detail: data }))
          }
        } catch (e) {
          // 非 JSON 消息忽略
        }
      }

      // 连接出错/关闭
      eventSource.onerror = () => {
        connected.value = false
        if (eventSource) {
          eventSource.close()
          eventSource = null
        }
        // SSE 连接断开后自动重连
        if (!isManuallyClosed) {
          console.log('[SSE] 连接断开，3秒后重连...')
          reconnectTimer = setTimeout(connect, 3000)
        }
      }
    } catch (e) {
      console.error('[SSE] 初始化失败:', e)
      if (!isManuallyClosed) {
        reconnectTimer = setTimeout(connect, 5000)
      }
    }
  }

  function disconnect() {
    isManuallyClosed = true
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    connected.value = false
    isManuallyClosed = false
  }

  onUnmounted(() => {
    disconnect()
  })

  return { connected, connect, disconnect }
}
