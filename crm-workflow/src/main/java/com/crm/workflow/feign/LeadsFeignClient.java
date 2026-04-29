package com.crm.workflow.feign;

import com.crm.common.result.Result;
import com.crm.workflow.feign.dto.LeadDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 线索管理 Feign 客户端
 */
@FeignClient(name = "crm-leads", path = "/leads/lead")
public interface LeadsFeignClient {

    /**
     * 查询线索详情
     */
    @GetMapping("/{id}")
    Result<LeadDTO> getLeadById(@PathVariable("id") Long id);

    /**
     * 更新线索（工作流触发分配时调用）
     */
    @PutMapping("/{id}")
    Result<Boolean> updateLead(@PathVariable("id") Long id, @RequestBody LeadDTO dto);
}
