package com.crm.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 网关鉴权配置
 */
@Configuration
public class SaTokenConfig {

    /**
     * 注册 Sa-Token 全局过滤器
     */
    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")
                // 开放地址（不鉴权的接口）
                .addExclude(
                        "/favicon.ico",
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/sse/**"
                )
                // 鉴权方法
                .setAuth(obj -> {
                    SaRouter.match("/**", StpUtil::checkLogin);
                });
    }
}
