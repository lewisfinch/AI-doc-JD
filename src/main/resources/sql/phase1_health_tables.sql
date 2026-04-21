-- AI健康问诊平台 - 阶段1核心表结构（MySQL 8+）
-- 执行前请先确认当前数据库：AIassistant

DROP TABLE IF EXISTS `health_report`;
DROP TABLE IF EXISTS `consultation_session`;
DROP TABLE IF EXISTS `patient`;

CREATE TABLE `patient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '当前登录用户ID',
  `name` VARCHAR(64) NOT NULL COMMENT '就诊人姓名',
  `age` INT NOT NULL COMMENT '年龄',
  `gender` VARCHAR(16) NOT NULL COMMENT '性别',
  `relation` VARCHAR(32) NOT NULL COMMENT '与当前用户关系',
  `medical_history` TEXT NULL COMMENT '既往病史',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_patient_user_id` (`user_id`),
  KEY `idx_patient_user_create_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就诊人表';

CREATE TABLE `consultation_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '当前登录用户ID',
  `patient_id` BIGINT NOT NULL COMMENT '就诊人ID',
  `memory_id` VARCHAR(128) NOT NULL COMMENT 'LangChain4j在MongoDB中的长记忆ID',
  `title` VARCHAR(255) NULL COMMENT 'AI自动生成摘要标题',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '会话状态：0-进行中，1-已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consultation_memory_id` (`memory_id`),
  KEY `idx_consultation_user_id` (`user_id`),
  KEY `idx_consultation_patient_id` (`patient_id`),
  KEY `idx_consultation_user_create_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问诊会话表';

CREATE TABLE `health_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '当前登录用户ID',
  `patient_id` BIGINT NOT NULL COMMENT '就诊人ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '上传文件名',
  `file_url` VARCHAR(500) NULL COMMENT '文件存储地址',
  `report_type` TINYINT NOT NULL COMMENT '报告类型：1-体检，2-化验，3-影像',
  `analysis_result` LONGTEXT NULL COMMENT '大模型生成的解读结论',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_report_user_id` (`user_id`),
  KEY `idx_report_patient_id` (`patient_id`),
  KEY `idx_report_user_patient` (`user_id`, `patient_id`),
  KEY `idx_report_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康报告表';