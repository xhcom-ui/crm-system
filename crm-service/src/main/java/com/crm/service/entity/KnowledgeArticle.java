package com.crm.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务知识库文章。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_knowledge_article")
public class KnowledgeArticle extends BaseEntity {

    private String title;
    private String category;
    private String tags;
    private Integer usedCount;
    private String solution;
    private String steps;
    private Integer status;
}
