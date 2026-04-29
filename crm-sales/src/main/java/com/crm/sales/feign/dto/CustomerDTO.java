package com.crm.sales.feign.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户信息 DTO（供 Feign 远程调用使用）
 */
@Data
public class CustomerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String company;
    private String position;
    private Integer source;
    private Integer level;
}
