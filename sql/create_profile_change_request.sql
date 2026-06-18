-- 个人资料修改申请表
CREATE TABLE IF NOT EXISTS `profile_change_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
  `field_name` VARCHAR(50) NOT NULL COMMENT '字段名: username / email',
  `old_value` VARCHAR(255) DEFAULT NULL COMMENT '旧值',
  `new_value` VARCHAR(255) NOT NULL COMMENT '新值',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待审核, 1=已通过, 2=已拒绝',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人资料修改申请';
