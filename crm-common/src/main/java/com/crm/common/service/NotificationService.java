package com.crm.common.service;

import com.crm.common.sse.SseEmitterManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一通知推送服务
 * 使用 SSE 推送通知。
 */
@Slf4j
@Service
public class NotificationService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 推送模式: sse */
    @Value("${notification.mode:sse}")
    private String mode;

    /**
     * 推送通知。
     */
    public void push(String title, String message, String type) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("message", message);
        payload.put("type", type != null ? type : "info");
        payload.put("timestamp", LocalDateTime.now().toString());

        String json;
        try {
            json = MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("通知序列化失败: {}", e.getMessage());
            return;
        }

        SseEmitterManager.broadcast("notification", json);
        log.debug("[通知-SSE] {} ({})", title, SseEmitterManager.getConnectionCount());
    }

    /**
     * 推送成功通知
     */
    public void pushSuccess(String title, String message) {
        push(title, message, "success");
    }

    /**
     * 推送警告通知
     */
    public void pushWarning(String title, String message) {
        push(title, message, "warning");
    }

    /**
     * 推送错误通知
     */
    public void pushError(String title, String message) {
        push(title, message, "error");
    }

    /**
     * 推送信息通知
     */
    public void pushInfo(String title, String message) {
        push(title, message, "info");
    }

    /**
     * 获取当前推送模式
     */
    public String getMode() {
        return mode;
    }

    /**
     * 获取连接统计
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("mode", mode);
        stats.put("sseConnections", SseEmitterManager.getConnectionCount());
        return stats;
    }
}
