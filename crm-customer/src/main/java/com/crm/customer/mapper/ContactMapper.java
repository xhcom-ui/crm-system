package com.crm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.customer.entity.Contact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户联系人 Mapper
 */
@Mapper
public interface ContactMapper extends BaseMapper<Contact> {
}
