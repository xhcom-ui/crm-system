package com.crm.service.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.result.Result;
import com.crm.service.entity.Ticket;
import com.crm.service.feign.CustomerFeignClient;
import com.crm.service.feign.dto.CustomerDTO;
import com.crm.service.service.TicketAssignService;
import com.crm.service.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/service/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CustomerFeignClient customerFeignClient;
    private final TicketAssignService ticketAssignService;

    @GetMapping("/page")
    @SaCheckPermission("service:list")
    public Result<IPage<Ticket>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(ticketService.pageTickets(new Page<>(current, size), keyword));
    }

    @GetMapping("/{id}")
    public Result<Ticket> getById(@PathVariable Long id) {
        return Result.success(ticketService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("service:add")
    public Result<Boolean> save(@Valid @RequestBody Ticket ticket) {
        // 新建工单自动分配处理人
        ticketAssignService.autoAssign(ticket);
        return Result.success(ticketService.save(ticket));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("service:edit")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody Ticket ticket) {
        ticket.setId(id);
        return Result.success(ticketService.updateById(ticket));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("service:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(ticketService.removeById(id));
    }

    /**
     * 手动重新分配工单
     */
    @PostMapping("/{id}/assign")
    public Result<Map<String, Object>> autoAssign(@PathVariable Long id) {
        Ticket ticket = ticketService.getById(id);
        if (ticket == null) {
            return Result.error("工单不存在");
        }
        Long oldAssignee = ticket.getAssigneeId();
        ticketAssignService.autoAssign(ticket);
        ticketService.updateById(ticket);

        Map<String, Object> result = new HashMap<>();
        result.put("ticketId", ticket.getId());
        result.put("oldAssignee", oldAssignee);
        result.put("newAssignee", ticket.getAssigneeId());
        result.put("status", ticket.getStatus());
        return Result.success(result);
    }

    /**
     * 查询工单详情（含关联客户信息 - Feign 跨服务调用）
     */
    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Ticket ticket = ticketService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticket);
        if (ticket != null && ticket.getContactId() != null) {
            try {
                Result<CustomerDTO> r = customerFeignClient.getContactById(ticket.getContactId());
                if (r != null && r.getData() != null) {
                    result.put("customer", r.getData());
                }
            } catch (Exception e) {
                result.put("customer", null);
            }
        }
        return Result.success(result);
    }

    /**
     * 获取处理人负载统计
     */
    @GetMapping("/assignee-load")
    public Result<Map<Long, Long>> assigneeLoad() {
        return Result.success(ticketAssignService.getAssigneeLoadStats());
    }
}
