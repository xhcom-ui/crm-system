package com.crm.leads.feign;

import com.crm.common.result.Result;
import com.crm.leads.feign.dto.OpportunityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 销售自动化 Feign 客户端
 */
@FeignClient(name = "crm-sales", path = "/sales/opportunity")
public interface SalesFeignClient {

    /**
     * 创建商机（线索转化时调用）
     */
    @PostMapping
    Result<Boolean> createOpportunity(@RequestBody OpportunityDTO dto);
}
