-- =====================================================
-- 竞赛管理系统 初始化脚本
-- =====================================================
DROP DATABASE IF EXISTS cms_competition;
CREATE DATABASE cms_competition DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cms_competition;

-- 1. 用户
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(50),
  `real_name` VARCHAR(50),
  `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT/TEACHER/ADMIN',
  `college` VARCHAR(100),
  `phone` VARCHAR(20),
  `email` VARCHAR(100),
  `avatar` VARCHAR(255),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 竞赛
CREATE TABLE `competition` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `category` VARCHAR(50),
  `type` TINYINT NOT NULL COMMENT '1个人 2团队',
  `max_team_size` INT DEFAULT 1,
  `min_team_size` INT DEFAULT 1,
  `register_start` DATETIME,
  `register_end` DATETIME,
  `comp_start` DATETIME,
  `comp_end` DATETIME,
  `cover` VARCHAR(255),
  `attachment` VARCHAR(255),
  `publisher_id` BIGINT,
  `teacher_id` BIGINT,
  `status` TINYINT DEFAULT 0 COMMENT '0下架 1上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  INDEX idx_status (status),
  INDEX idx_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 团队
CREATE TABLE `team` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `team_name` VARCHAR(100) NOT NULL,
  `competition_id` BIGINT NOT NULL,
  `captain_id` BIGINT NOT NULL,
  `invite_code` VARCHAR(10) NOT NULL UNIQUE,
  `max_size` INT DEFAULT 5,
  `slogan` VARCHAR(255),
  `status` TINYINT DEFAULT 0 COMMENT '0组建中 1已提交 2已锁定',
  `dissolved_at` DATETIME COMMENT '解散时间，12小时内队长可恢复',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  INDEX idx_competition (competition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 团队成员
CREATE TABLE `team_member` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `team_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `role` TINYINT DEFAULT 0 COMMENT '0队员 1队长',
  `status` TINYINT DEFAULT 0 COMMENT '0待审核 1已加入 2已拒绝',
  `join_time` DATETIME,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_team_user (team_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 个人报名
CREATE TABLE `registration` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `competition_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝',
  `attachment` VARCHAR(255),
  `description` TEXT,
  `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `reviewer_id` BIGINT,
  `review_remark` VARCHAR(500),
  `review_time` DATETIME,
  `is_deleted` TINYINT DEFAULT 0,
  UNIQUE KEY uk_comp_user (competition_id, user_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 团队报名
CREATE TABLE `team_registration` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `competition_id` BIGINT NOT NULL,
  `team_id` BIGINT NOT NULL,
  `status` TINYINT DEFAULT 0,
  `attachment` VARCHAR(255),
  `description` TEXT,
  `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `reviewer_id` BIGINT,
  `review_remark` VARCHAR(500),
  `review_time` DATETIME,
  `is_deleted` TINYINT DEFAULT 0,
  UNIQUE KEY uk_comp_team (competition_id, team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 轮播图
CREATE TABLE `banner` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200),
  `image_url` VARCHAR(255) NOT NULL,
  `link_url` VARCHAR(255),
  `sort` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `start_time` DATETIME,
  `end_time` DATETIME,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 热门推荐
CREATE TABLE `hot_recommend` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `competition_id` BIGINT NOT NULL UNIQUE,
  `sort` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 消息
CREATE TABLE `message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(50),
  `title` VARCHAR(200),
  `content` TEXT,
  `is_read` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 团队日志
CREATE TABLE `team_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `team_id` BIGINT NOT NULL,
  `operator_id` BIGINT,
  `action` VARCHAR(50),
  `detail` VARCHAR(500),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 团队指导老师邀请
CREATE TABLE `team_advisor` (
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

-- 12. 个人资料修改申请
CREATE TABLE IF NOT EXISTS `profile_change_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
  `field_name` VARCHAR(50) NOT NULL COMMENT '字段名: username / email',
  `old_value` VARCHAR(255) DEFAULT NULL COMMENT '旧值',
  `new_value` VARCHAR(255) NOT NULL COMMENT '新值',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人资料修改申请';

-- =====================================================
-- 测试数据（密码统一为 123456）
-- BCrypt: $2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu
-- =====================================================
INSERT INTO `user`(username,password,nickname,real_name,role,college,phone,avatar) VALUES
('admin',   '$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu', '超级管理员', '管理员',  'ADMIN',   'XX大学', '13800000000', '/images/avatars/avatar1.png'),
('teacher1','$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu', '张老师',     '张老师',  'TEACHER', 'XX大学', '13800000001', '/images/avatars/avatar2.png'),
('teacher2','$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu', '李老师',     '李老师',  'TEACHER', 'XX大学', '13800000002', '/images/avatars/avatar3.png'),
('stu1',    '$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu', '小明',       '王小明',  'STUDENT', 'XX大学', '13800000003', '/images/avatars/avatar4.png'),
('stu2',    '$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu', '小红',       '李小红',  'STUDENT', 'XX大学', '13800000004', '/images/avatars/avatar5.png'),
('stu3',    '$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu', '小华',       '张小华',  'STUDENT', 'XX大学', '13800000005', '/images/avatars/avatar6.png');

INSERT INTO `competition`(title,description,category,type,max_team_size,min_team_size,register_start,register_end,cover,publisher_id,teacher_id,status) VALUES
('全国大学生数学建模竞赛', '全国性数学建模赛事，欢迎组队参加', '学科竞赛', 2, 3, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', '/images/covers/cover1.png', 1, 2, 1),
('互联网+创新创业大赛',     '创新创业大赛，个人赛',          '创新创业', 1, 1, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', '/images/covers/cover2.png', 1, 3, 1),
('ACM 程序设计竞赛',        '编程类团队赛，每队 3 人',      '学科竞赛', 2, 3, 2, '2026-06-01 00:00:00', '2026-12-31 23:59:59', '/images/covers/cover3.png', 1, 2, 1),
('英语演讲比赛',            '个人赛，提升英语表达',          '文体艺术', 1, 1, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', '/images/covers/cover4.png', 1, 2, 0),
('蓝桥杯全国软件和信息技术专业人才大赛', '面向全国高校学生的软件与信息技术类竞赛，涵盖程序设计、Web开发、数据库等多个赛道', '学科竞赛', 1, 1, 1, '2026-03-01 00:00:00', '2026-05-15 23:59:59', '/images/covers/cover5.png', 1, 2, 1),
('全国大学生数学竞赛', '由中国数学会主办的全国性大学生数学赛事，考察高等数学、线性代数、概率论等核心数学知识', '学科竞赛', 1, 1, 1, '2026-04-01 00:00:00', '2026-05-31 23:59:59', '/images/covers/cover6.png', 1, 3, 1),
('中国大学生程序设计竞赛', 'CCPC 全国性程序设计赛事，以团队形式参赛，每队3人，在限定时间内解决算法与数据结构问题', '学科竞赛', 2, 3, 3, '2026-07-01 00:00:00', '2026-09-30 23:59:59', '/images/covers/cover7.png', 1, 2, 1),
('全国大学生电子设计竞赛', '教育部倡导的全国性大学生电子类竞赛，以团队形式完成电子系统设计与制作', '学科竞赛', 2, 3, 2, '2026-05-01 00:00:00', '2026-07-15 23:59:59', '/images/covers/cover8.png', 1, 2, 1),
('嵌入式系统设计竞赛', '面向全国高校学生的嵌入式系统设计与开发竞赛，以团队形式完成嵌入式软硬件系统的设计、实现与优化', '学科竞赛', 2, 4, 2, '2026-08-01 00:00:00', '2026-10-31 23:59:59', '/images/covers/cover9.png', 1, 3, 1),
('RoboMaster机甲大师赛', '由大疆发起的机器人竞技赛事，参赛队伍设计并制作多种功能的机器人，在赛场中进行对抗比赛', '科技创新', 2, 5, 3, '2026-09-01 00:00:00', '2026-12-31 23:59:59', '/images/covers/cover10.png', 1, 2, 1);

INSERT INTO `banner`(title,image_url,link_url,sort,status) VALUES
('数学建模竞赛火热报名中', '/images/banners/banner1.png', '/competitions/1', 1, 1),
('互联网+大赛启动',         '/images/banners/banner2.png', '/competitions/2', 2, 1),
('ACM程序设计竞赛',         '/images/banners/banner3.png', '/competitions/3', 3, 1),
('英语演讲比赛',            '/images/banners/banner4.png', '/competitions/4', 4, 1),
('创新创业大赛等你来',       '/images/banners/banner5.png', '/competitions/2', 5, 1);

INSERT INTO `hot_recommend`(competition_id,sort) VALUES
(1, 100),
(3, 90);
