package com.crm.common.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SSE 连接管理器
 * 管理所有 SSE 客户端连接，支持单播和广播
 */
@Slf4j
public class SseEmitterManager {

    /** 已连接客户端 (clientId -> SseEmitter) */
    private static final Map<String, SseEmitter> CLIENTS = new ConcurrentHashMap<>();

    /** 客户端 ID 生成器 */
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    /** SSE 超时时间 (30分钟) */
    private static final long TIMEOUT = 30 * 60 * 1000L;

    /**
     * 创建并注册新的 SSE 连接
     */
    public static SseEmitter createEmitter() {
        String clientId = "sse-" + ID_GENERATOR.incrementAndGet();
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> {
            CLIENTS.remove(clientId);
            log.info("SSE 连接完成: {}", clientId);
        });

        emitter.onTimeout(() -> {
            CLIENTS.remove(clientId);
            log.info("SSE 连接超时: {}", clientId);
        });

        emitter.onError(e -> {
            CLIENTS.remove(clientId);
            log.warn("SSE 连接异常: {} - {}", clientId, e.getMessage());
        });

        CLIENTS.put(clientId, emitter);

        // 发送连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"type\":\"connected\",\"clientId\":\"" + clientId + "\"}"));
        } catch (IOException e) {
            log.error("SSE 发送连接确认失败: {}", e.getMessage());
        }

        log.info("SSE 连接建立: {} (当前连接数: {})", clientId, CLIENTS.size());
        return emitter;
    }

    /**
     * 向所有连接的客户端广播消息
     */
    public static void broadcast(String eventName, String data) {
        CLIENTS.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.error("SSE 发送失败 [{}]: {}", clientId, e.getMessage());
                CLIENTS.remove(clientId);
            }
        });
    }

    /**
     * 向特定客户端发送消息
     */
    public static void sendToClient(String clientId, String eventName, String data) {
        SseEmitter emitter = CLIENTS.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                log.error("SSE 单播失败 [{}]: {}", clientId, e.getMessage());
                CLIENTS.remove(clientId);
            }
        }
    }

    /**
     * 获取当前连接数
     */
    public static int getConnectionCount() {
        return CLIENTS.size();
    }
}
