package com.crm.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "crm.gateway.sentinel")
public class CrmGatewaySentinelProperties {

    /**
     * Whether local gateway flow rules should be loaded on startup.
     */
    private boolean enabled = true;

    private List<RouteRule> routeRules = new ArrayList<>();

    private List<ApiRule> apiRules = new ArrayList<>();

    @Data
    public static class RouteRule {
        private String routeId;
        private String path;
        private double qps;
        private int burst = 0;
    }

    @Data
    public static class ApiRule {
        private String apiName;
        private String path;
        private String matchStrategy = "exact";
        private double qps;
        private int burst = 0;
    }
}
