package com.crm.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.auth.entity.Notification;
import com.crm.auth.entity.NotificationChannelConfig;
import com.crm.auth.entity.NotificationPushLog;
import com.crm.auth.mapper.NotificationChannelConfigMapper;
import com.crm.auth.mapper.NotificationMapper;
import com.crm.auth.mapper.NotificationPushLogMapper;
import com.crm.auth.service.NotificationChannelPushService;
import com.crm.common.log.OperationLog;
import com.crm.common.service.NotificationService;
import com.crm.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知管理 Controller
 */
@RestController
@RequestMapping("/system/notification")
@RequiredArgsConstructor
public class NotificationManageController {

    private final NotificationMapper notificationMapper;
    private final NotificationChannelConfigMapper channelConfigMapper;
    private final NotificationPushLogMapper pushLogMapper;
    private final NotificationService notificationService;
    private final NotificationChannelPushService channelPushService;

    /**
     * 发送广播通知
     */
    @PostMapping("/send")
    @SaCheckPermission("system:notif:send")
    @OperationLog(title = "发送通知", type = "SEND")
    public Result<Map<String, Object>> send(@RequestBody SendNotificationRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setSender("用户" + userId);
        notification.setChannels(String.join(",", request.selectedChannels()));

        // 保存记录
        notificationMapper.insert(notification);

        // 实时推送
        notificationService.push(notification.getTitle(),
                notification.getMessage(), notification.getType());

        List<NotificationPushLog> logs = channelPushService.push(notification, request.selectedChannels(), request.getReceiver());
        notification.setPushSummary(channelPushService.summarize(logs));
        notificationMapper.updateById(notification);
        return Result.success(Map.of("summary", notification.getPushSummary(), "logs", logs));
    }

    /**
     * 通知历史记录（分页）
     */
    @GetMapping("/page")
    @SaCheckPermission("system:notif:send")
    public Result<Page<Notification>> page(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size) {
        Page<Notification> page = notificationMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<Notification>().orderByDesc(Notification::getCreateTime));
        return Result.success(page);
    }

    @GetMapping("/channels")
    @SaCheckPermission("system:notif:send")
    public Result<List<NotificationChannelConfig>> channels() {
        List<NotificationChannelConfig> configs = channelConfigMapper.selectList(
                new LambdaQueryWrapper<NotificationChannelConfig>().orderByAsc(NotificationChannelConfig::getId));
        return Result.success(configs);
    }

    @PutMapping("/channels/{id}")
    @SaCheckPermission("system:notif:send")
    @OperationLog(title = "配置通知渠道", type = "UPDATE")
    public Result<Void> updateChannel(@PathVariable Long id, @RequestBody NotificationChannelConfig config) {
        config.setId(id);
        channelConfigMapper.updateById(config);
        return Result.success();
    }

    @GetMapping("/{id}/push-logs")
    @SaCheckPermission("system:notif:send")
    public Result<List<NotificationPushLog>> pushLogs(@PathVariable Long id) {
        List<NotificationPushLog> logs = pushLogMapper.selectList(new LambdaQueryWrapper<NotificationPushLog>()
                .eq(NotificationPushLog::getNotificationId, id)
                .orderByDesc(NotificationPushLog::getCreateTime));
        return Result.success(logs);
    }

    @lombok.Data
    public static class SendNotificationRequest {
        private String title;
        private String message;
        private String type = "info";
        private String receiver;
        private List<String> channels;

        public List<String> selectedChannels() {
            if (channels == null || channels.isEmpty()) {
                return List.of();
            }
            return channels.stream()
                    .flatMap(item -> Arrays.stream(item.split(",")))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty() && !"sse".equals(item))
                    .distinct()
                    .collect(Collectors.toList());
        }
    }
}
