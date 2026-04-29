package com.crm.sales.feign;

import com.crm.common.result.Result;
import com.crm.sales.feign.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 客户管理 Feign 客户端
 */
@FeignClient(name = "crm-customer", path = "/customer/contact")
public interface CustomerFeignClient {

    /**
     * 查询客户详情
     */
    @GetMapping("/{id}")
    Result<CustomerDTO> getContactById(@PathVariable("id") Long id);
}
