package com.crm.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.service.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}
