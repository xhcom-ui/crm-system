package com.crm.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.service.entity.Ticket;

public interface TicketService extends IService<Ticket> {
    IPage<Ticket> pageTickets(Page<Ticket> page, String keyword);
}
