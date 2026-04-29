-- =============================================
-- CRM 分库分表物理表样例
-- 在执行前请先完成 sql/crm_init.sql 和 sql/crm_upgrade_insights.sql
-- =============================================

CREATE DATABASE IF NOT EXISTS crm_auth_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_auth_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_customer_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_customer_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_service_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_service_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_marketing_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_marketing_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 操作日志：2 库 x 16 表
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_0 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_1 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_2 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_3 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_4 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_5 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_6 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_7 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_8 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_9 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_10 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_11 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_12 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_13 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_14 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_0.sys_oper_log_15 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_0 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_1 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_2 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_3 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_4 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_5 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_6 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_7 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_8 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_9 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_10 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_11 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_12 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_13 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_14 LIKE crm_auth.sys_oper_log;
CREATE TABLE IF NOT EXISTS crm_auth_1.sys_oper_log_15 LIKE crm_auth.sys_oper_log;

-- 客户互动、跟进、工单、营销效果：2 库 x 8 表
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_0 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_1 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_2 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_3 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_4 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_5 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_6 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_customer_interaction_7 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_0 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_1 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_2 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_3 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_4 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_5 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_6 LIKE crm_customer.crm_customer_interaction;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_customer_interaction_7 LIKE crm_customer.crm_customer_interaction;

CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_0 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_1 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_2 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_3 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_4 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_5 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_6 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_0.crm_followup_record_7 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_0 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_1 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_2 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_3 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_4 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_5 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_6 LIKE crm_customer.crm_followup_record;
CREATE TABLE IF NOT EXISTS crm_customer_1.crm_followup_record_7 LIKE crm_customer.crm_followup_record;

CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_0 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_1 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_2 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_3 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_4 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_5 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_6 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_0.crm_ticket_7 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_0 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_1 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_2 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_3 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_4 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_5 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_6 LIKE crm_service.crm_ticket;
CREATE TABLE IF NOT EXISTS crm_service_1.crm_ticket_7 LIKE crm_service.crm_ticket;

CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_0 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_1 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_2 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_3 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_4 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_5 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_6 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_0.crm_campaign_performance_7 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_0 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_1 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_2 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_3 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_4 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_5 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_6 LIKE crm_marketing.crm_campaign_performance;
CREATE TABLE IF NOT EXISTS crm_marketing_1.crm_campaign_performance_7 LIKE crm_marketing.crm_campaign_performance;
