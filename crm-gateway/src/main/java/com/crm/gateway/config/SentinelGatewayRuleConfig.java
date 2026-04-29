package com.crm.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CrmGatewaySentinelProperties.class)
public class SentinelGatewayRuleConfig {

    private final CrmGatewaySentinelProperties properties;

    @PostConstruct
    public void initGatewayRules() {
        registerFallback();
        if (!properties.isEnabled()) {
            return;
        }
        registerApiDefinitions();
        registerRouteRules();
    }

    private void registerApiDefinitions() {
        Set<ApiDefinition> definitions = properties.getRouteRules().stream()
                .filter(rule -> hasText(rule.getRouteId()) && hasText(rule.getPath()))
                .map(rule -> new ApiDefinition(rule.getRouteId())
                        .setPredicateItems(Set.of(new ApiPathPredicateItem()
                                .setPattern(rule.getPath())
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX))))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        properties.getApiRules().stream()
                .filter(rule -> hasText(rule.getApiName()) && hasText(rule.getPath()))
                .map(rule -> new ApiDefinition(rule.getApiName())
                        .setPredicateItems(Set.of(new ApiPathPredicateItem()
                                .setPattern(rule.getPath())
                                .setMatchStrategy(resolveUrlMatchStrategy(rule.getMatchStrategy())))))
                .forEach(definitions::add);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void registerRouteRules() {
        Set<GatewayFlowRule> rules = properties.getRouteRules().stream()
                .filter(rule -> hasText(rule.getRouteId()) && rule.getQps() > 0)
                .map(rule -> new GatewayFlowRule(rule.getRouteId())
                        .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID)
                        .setGrade(RuleConstant.FLOW_GRADE_QPS)
                        .setCount(rule.getQps())
                        .setBurst(rule.getBurst())
                        .setIntervalSec(1))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        properties.getApiRules().stream()
                .filter(rule -> hasText(rule.getApiName()) && rule.getQps() > 0)
                .map(rule -> new GatewayFlowRule(rule.getApiName())
                        .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                        .setGrade(RuleConstant.FLOW_GRADE_QPS)
                        .setCount(rule.getQps())
                        .setBurst(rule.getBurst())
                        .setIntervalSec(1))
                .forEach(rules::add);
        GatewayRuleManager.loadRules(rules);
    }

    private void registerFallback() {
        GatewayCallbackManager.setBlockHandler((exchange, throwable) -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("code", 429);
            body.put("message", "当前访问量较高，请稍后重试");
            body.put("data", null);
            body.put("timestamp", System.currentTimeMillis());
            body.put("path", exchange.getRequest().getPath().value());
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        });
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int resolveUrlMatchStrategy(String matchStrategy) {
        if ("prefix".equalsIgnoreCase(matchStrategy)) {
            return SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX;
        }
        if ("regex".equalsIgnoreCase(matchStrategy)) {
            return SentinelGatewayConstants.URL_MATCH_STRATEGY_REGEX;
        }
        return SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT;
    }
}
