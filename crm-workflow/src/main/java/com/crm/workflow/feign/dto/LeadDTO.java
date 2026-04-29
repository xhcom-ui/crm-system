package com.crm.workflow.feign.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 线索 DTO（供工作流触发线索分配使用）
 */
@Data
public class LeadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String contactInfo;
    private Integer source;
    private Integer score;
    private Integer status;
    private Long assigneeId;
}
