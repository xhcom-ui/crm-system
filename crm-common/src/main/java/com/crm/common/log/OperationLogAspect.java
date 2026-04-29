package com.crm.common.log;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${crm.operation-log.endpoint:http://127.0.0.1:8087/system/oper-log/internal}")
    private String endpoint;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        String user = currentUser();
        HttpServletRequest request = currentRequest();
        try {
            Object result = point.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("operation title={} type={} user={} method={} url={} status=success cost={}ms",
                    operationLog.title(), operationLog.type(), user, method(request), uri(request), cost);
            submit(operationLog, user, request, point.getArgs(), result, null, cost);
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            log.warn("operation title={} type={} user={} method={} url={} status=fail cost={}ms error={}",
                    operationLog.title(), operationLog.type(), user, method(request), uri(request),
                    cost, ex.getMessage());
            submit(operationLog, user, request, point.getArgs(), null, ex, cost);
            throw ex;
        }
    }

    private void submit(OperationLog operationLog, String user, HttpServletRequest request, Object[] args, Object result, Throwable ex, long cost) {
        if (endpoint == null || endpoint.isBlank()) return;
        OperationLogRecord record = new OperationLogRecord();
        record.setTitle(operationLog.title());
        record.setOperType(operationLog.type());
        record.setOperName(user);
        record.setRequestUrl(uri(request));
        record.setRequestMethod(method(request));
        record.setRequestParams(limit(Arrays.toString(args)));
        record.setJsonResult(limit(String.valueOf(result)));
        record.setStatus(ex == null ? 1 : 0);
        record.setErrorMsg(ex == null ? null : limit(ex.getMessage()));
        record.setCostTime(cost);
        record.setOperIp(ip(request));
        CompletableFuture.runAsync(() -> {
            try {
                restTemplate.postForObject(endpoint, record, String.class);
            } catch (Exception e) {
                log.debug("submit operation log failed: {}", e.getMessage());
            }
        });
    }

    private String currentUser() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "anonymous";
        } catch (Exception ignored) {
            return "anonymous";
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String method(HttpServletRequest request) {
        return request == null ? "" : request.getMethod();
    }

    private String uri(HttpServletRequest request) {
        return request == null ? "" : request.getRequestURI();
    }

    private String ip(HttpServletRequest request) {
        if (request == null) return "";
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private String limit(String value) {
        if (value == null) return null;
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }
}
