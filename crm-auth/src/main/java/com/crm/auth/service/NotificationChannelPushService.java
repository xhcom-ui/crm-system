package com.crm.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.auth.entity.Notification;
import com.crm.auth.entity.NotificationChannelConfig;
import com.crm.auth.entity.NotificationPushLog;
import com.crm.auth.mapper.NotificationChannelConfigMapper;
import com.crm.auth.mapper.NotificationPushLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 外部通知渠道推送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationChannelPushService {

    private final NotificationChannelConfigMapper configMapper;
    private final NotificationPushLogMapper pushLogMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<NotificationPushLog> push(Notification notification, List<String> channels) {
        return push(notification, channels, null);
    }

    public List<NotificationPushLog> push(Notification notification, List<String> channels, String receiverOverride) {
        if (channels == null || channels.isEmpty()) {
            return Collections.emptyList();
        }
        List<NotificationPushLog> logs = new ArrayList<>();
        for (String channel : channels.stream().filter(StringUtils::hasText).distinct().toList()) {
            NotificationChannelConfig config = configMapper.selectOne(new LambdaQueryWrapper<NotificationChannelConfig>()
                    .eq(NotificationChannelConfig::getChannel, channel)
                    .last("LIMIT 1"));
            logs.add(pushOne(notification, channel, config, receiverOverride));
        }
        return logs;
    }

    public String summarize(List<NotificationPushLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return "站内通知已发送";
        }
        Map<String, Long> grouped = logs.stream().collect(Collectors.groupingBy(NotificationPushLog::getStatus, Collectors.counting()));
        return String.format("成功 %d，跳过 %d，失败 %d",
                grouped.getOrDefault("SUCCESS", 0L),
                grouped.getOrDefault("SKIPPED", 0L),
                grouped.getOrDefault("FAILED", 0L));
    }

    private NotificationPushLog pushOne(Notification notification, String channel, NotificationChannelConfig config, String receiverOverride) {
        NotificationPushLog logRecord = new NotificationPushLog();
        logRecord.setNotificationId(notification.getId());
        logRecord.setChannel(channel);
        logRecord.setChannelName(config != null ? config.getChannelName() : channel);
        String receiver = config == null ? receiverOverride : firstText(receiverOverride, config.getReceiver());
        logRecord.setTarget(config != null ? firstText(receiver, config.getWebhookUrl(), config.getSenderAddress()) : receiver);

        try {
            if (config == null || config.getEnabled() == null || config.getEnabled() != 1) {
                return save(logRecord, "SKIPPED", "渠道未配置或未启用");
            }
            if ("email".equals(channel)) {
                return pushEmail(notification, config, logRecord, receiver);
            }
            if ("sms".equals(channel) && !StringUtils.hasText(receiver)) {
                return save(logRecord, "SKIPPED", "短信接收人未配置");
            }
            if (!StringUtils.hasText(config.getWebhookUrl())) {
                return save(logRecord, "SKIPPED", "未配置 webhook/API 地址");
            }
            Map<String, Object> body = buildWebhookPayload(channel, notification, config, receiver);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String response = restTemplate.postForObject(config.getWebhookUrl(), new HttpEntity<>(body, headers), String.class);
            return save(logRecord, "SUCCESS", truncate(response));
        } catch (Exception e) {
            log.warn("通知渠道推送失败 channel={}: {}", channel, e.getMessage());
            return save(logRecord, "FAILED", truncate(e.getMessage()));
        }
    }

    private NotificationPushLog pushEmail(Notification notification, NotificationChannelConfig config, NotificationPushLog logRecord, String receiver) {
        if (!StringUtils.hasText(config.getSmtpHost()) || !StringUtils.hasText(receiver)
                || !StringUtils.hasText(config.getSenderAddress())) {
            return save(logRecord, "SKIPPED", "邮件 SMTP 或收件人未配置");
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort() == null ? 25 : config.getSmtpPort());
        sender.setUsername(config.getSmtpUsername());
        sender.setPassword(config.getSmtpPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(StringUtils.hasText(config.getSmtpUsername())));
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.connectiontimeout", "5000");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(config.getSenderAddress());
        message.setTo(Arrays.stream(receiver.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toArray(String[]::new));
        message.setSubject(notification.getTitle());
        message.setText(renderContent(notification, config.getTemplate()));
        sender.send(message);
        return save(logRecord, "SUCCESS", "邮件已发送: " + receiver);
    }

    private Map<String, Object> buildWebhookPayload(String channel, Notification notification, NotificationChannelConfig config, String receiver) {
        String content = renderContent(notification, config.getTemplate());
        if ("dingtalk".equals(channel)) {
            return Map.of("msgtype", "text", "text", Map.of("content", content));
        }
        if ("feishu".equals(channel)) {
            return Map.of("msg_type", "text", "content", Map.of("text", content));
        }
        if ("wechat".equals(channel)) {
            return Map.of("msgtype", "text", "text", Map.of("content", content));
        }
        if ("sms".equals(channel)) {
            return Map.of("phones", receiver, "content", content);
        }
        return Map.of("title", notification.getTitle(), "message", notification.getMessage(), "type", notification.getType());
    }

    private String renderContent(Notification notification, String template) {
        String text = StringUtils.hasText(template) ? template : "【${title}】${message}";
        return text.replace("${title}", safe(notification.getTitle()))
                .replace("${message}", safe(notification.getMessage()))
                .replace("${type}", safe(notification.getType()));
    }

    private NotificationPushLog save(NotificationPushLog logRecord, String status, String response) {
        logRecord.setStatus(status);
        logRecord.setResponse(response);
        pushLogMapper.insert(logRecord);
        return logRecord;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) return value;
        }
        return "";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value) {
        if (value == null) return "";
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
