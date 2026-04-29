package com.crm.leads;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.crm.leads", "com.crm.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.crm.leads.mapper")
public class LeadsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeadsApplication.class, args);
    }
}
