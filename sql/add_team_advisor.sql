-- =====================================================
-- 新增：团队指导老师邀请表
-- =====================================================

CREATE TABLE IF NOT EXISTS `team_advisor` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `team_id` BIGINT NOT NULL COMMENT '团队ID',
  `teacher_id` BIGINT NOT NULL COMMENT '教师用户ID',
  `status` TINYINT DEFAULT 0 COMMENT '0待审核 1已接受 2已拒绝',
  `invited_by` BIGINT COMMENT '邀请人(队长)userId',
  `invite_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
  `accept_time` DATETIME COMMENT '接受时间',
  `reject_time` DATETIME COMMENT '拒绝时间',
  `remark` VARCHAR(500) COMMENT '邀请备注',
  INDEX idx_team (team_id),
  INDEX idx_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
