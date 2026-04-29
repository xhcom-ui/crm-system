package com.crm.common.controller;

import com.crm.common.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * SSE (Server-Sent Events) 推送控制器
 * 提供实时通知推送能力
 */
@Slf4j
@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseController {

    /**
     * SSE 订阅端点 - 客户端通过 EventSource 连接
     * GET /api/sse/notification/subscribe
     */
    @GetMapping("/notification/subscribe")
    public SseEmitter subscribe() {
        log.info("SSE 订阅请求");
        return SseEmitterManager.createEmitter();
    }

    /**
     * 获取当前 SSE 连接状态
     */
    @GetMapping("/notification/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("connections", SseEmitterManager.getConnectionCount());
        result.put("protocol", "SSE");
        return result;
    }
}
