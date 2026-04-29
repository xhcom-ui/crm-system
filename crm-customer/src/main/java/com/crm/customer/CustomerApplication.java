package com.crm.customer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 客户管理微服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.crm.customer", "com.crm.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.crm.customer.mapper")
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
