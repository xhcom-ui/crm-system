import { ref, watch } from 'vue'
import { useSSE } from './useSSE'

/**
 * 统一通知客户端 composable
 * 当前使用 SSE 推送模式。
 */
export function useNotification() {
  localStorage.setItem('notification_mode', 'sse')

  const mode = ref('sse')
  const sseClient = useSSE()
  const connected = ref(false)

  function connect() {
    sseClient.connect()
  }

  function disconnect() {
    sseClient.disconnect()
    connected.value = false
  }

  function setMode(newMode) {
    if (newMode !== 'sse') console.warn('[通知] 已固定使用 SSE 推送')
    mode.value = 'sse'
    localStorage.setItem('notification_mode', 'sse')
  }

  function getMode() {
    return mode.value
  }

  watch(() => sseClient.connected.value, (v) => {
    connected.value = v
  })

  return {
    connected,
    mode,
    connect,
    disconnect,
    setMode,
    getMode,
  }
}
