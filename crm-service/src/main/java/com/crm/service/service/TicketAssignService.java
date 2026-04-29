package com.crm.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.service.entity.Ticket;
import com.crm.service.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 工单自动分配服务
 * 按优先级 + 负载均衡分配处理人
 */
@Service
@RequiredArgsConstructor
public class TicketAssignService {

    private final TicketMapper ticketMapper;

    /**
     * 自动分配工单给最空闲的处理人
     * @param ticket 待分配的工单
     * @return 分配后的工单
     */
    public Ticket autoAssign(Ticket ticket) {
        // 统计每个处理人当前的工单数（处理中状态=2）
        List<Ticket> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus, 2)
                        .isNotNull(Ticket::getAssigneeId));

        // 按处理人分组统计
        Map<Long, Long> assigneeLoad = new HashMap<>();
        for (Ticket t : tickets) {
            if (t.getAssigneeId() != null) {
                assigneeLoad.merge(t.getAssigneeId(), 1L, Long::sum);
            }
        }

        // 找到负载最小的处理人
        Long bestAssignee = assigneeLoad.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1L); // 默认分配给ID=1的用户

        // 高优先级工单优先分配
        if (ticket.getPriority() != null && ticket.getPriority() >= 3) {
            // 高优先级工单分配给负载最小的
            ticket.setAssigneeId(bestAssignee);
            ticket.setStatus(2); // 处理中
        } else {
            ticket.setAssigneeId(bestAssignee);
            ticket.setStatus(1); // 待处理
        }

        return ticket;
    }

    /**
     * 获取处理人负载统计
     */
    public Map<Long, Long> getAssigneeLoadStats() {
        List<Ticket> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<Ticket>()
                        .in(Ticket::getStatus, 1, 2)
                        .isNotNull(Ticket::getAssigneeId));

        Map<Long, Long> load = new HashMap<>();
        for (Ticket t : tickets) {
            if (t.getAssigneeId() != null) {
                load.merge(t.getAssigneeId(), 1L, Long::sum);
            }
        }
        return load;
    }
}
