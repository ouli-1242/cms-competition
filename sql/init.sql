-- =====================================================
-- 高校学科竞赛报名管理系统 — 数据库初始化脚本
-- 与实际数据库结构完全对齐（2026-06-28 重建）
-- =====================================================
DROP DATABASE IF EXISTS cms_competition;
CREATE DATABASE cms_competition DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cms_competition;

-- 1. 用户
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT NULL,
  `real_name` VARCHAR(50) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT/TEACHER/ADMIN',
  `college` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `last_team_dissolve_time` DATETIME DEFAULT NULL COMMENT '上次团队被解散的时间（12小时冷却期）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 竞赛
CREATE TABLE `competition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `category` VARCHAR(50) DEFAULT NULL,
  `type` TINYINT NOT NULL COMMENT '1个人 2团队',
  `max_team_size` INT DEFAULT 1,
  `min_team_size` INT DEFAULT 1,
  `register_start` DATETIME DEFAULT NULL,
  `register_end` DATETIME DEFAULT NULL,
  `comp_start` DATETIME DEFAULT NULL,
  `comp_end` DATETIME DEFAULT NULL,
  `cover` VARCHAR(255) DEFAULT NULL,
  `attachment` VARCHAR(255) DEFAULT NULL,
  `publisher_id` BIGINT DEFAULT NULL,
  `teacher_id` BIGINT DEFAULT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0下架 1上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 团队
CREATE TABLE `team` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `team_name` VARCHAR(100) NOT NULL,
  `competition_id` BIGINT NOT NULL,
  `captain_id` BIGINT NOT NULL,
  `invite_code` VARCHAR(10) NOT NULL,
  `max_size` INT DEFAULT 5,
  `slogan` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0组建中 1已提交 2已锁定',
  `dissolved_at` DATETIME DEFAULT NULL COMMENT '解散时间，12小时内队长可恢复',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `invite_code` (`invite_code`),
  KEY `idx_competition` (`competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 团队成员
CREATE TABLE `team_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `team_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `role` TINYINT DEFAULT 0 COMMENT '0队员 1队长',
  `status` TINYINT DEFAULT 0 COMMENT '0待审核 1已加入 2已拒绝',
  `join_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_user` (`team_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 个人报名
CREATE TABLE `registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `competition_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝',
  `attachment` VARCHAR(255) DEFAULT NULL,
  `description` TEXT,
  `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `reviewer_id` BIGINT DEFAULT NULL,
  `review_remark` VARCHAR(500) DEFAULT NULL,
  `review_time` DATETIME DEFAULT NULL,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comp_user` (`competition_id`, `user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 团队报名
CREATE TABLE `team_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `competition_id` BIGINT NOT NULL,
  `team_id` BIGINT NOT NULL,
  `status` TINYINT DEFAULT 0,
  `attachment` VARCHAR(255) DEFAULT NULL,
  `description` TEXT,
  `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `reviewer_id` BIGINT DEFAULT NULL,
  `review_remark` VARCHAR(500) DEFAULT NULL,
  `review_time` DATETIME DEFAULT NULL,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comp_team` (`competition_id`, `team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 轮播图
CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) DEFAULT NULL,
  `image_url` VARCHAR(255) NOT NULL,
  `link_url` VARCHAR(255) DEFAULT NULL,
  `sort` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 热门推荐
CREATE TABLE `hot_recommend` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `competition_id` BIGINT NOT NULL,
  `sort` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `competition_id` (`competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 消息
CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(50) DEFAULT NULL,
  `title` VARCHAR(200) DEFAULT NULL,
  `content` TEXT,
  `is_read` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 团队指导老师邀请
CREATE TABLE `team_advisor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `team_id` BIGINT NOT NULL COMMENT '团队ID',
  `teacher_id` BIGINT NOT NULL COMMENT '教师用户ID',
  `status` TINYINT DEFAULT 0 COMMENT '0待审核 1已接受 2已拒绝',
  `invited_by` BIGINT DEFAULT NULL COMMENT '邀请人(队长)userId',
  `invite_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '接受时间',
  `reject_time` DATETIME DEFAULT NULL COMMENT '拒绝时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '邀请备注',
  PRIMARY KEY (`id`),
  KEY `idx_team` (`team_id`),
  KEY `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 个人资料修改申请
CREATE TABLE `profile_change_request` (
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
-- 测试数据
-- 密码统一为 123456
-- BCrypt: $2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu
-- stu2 密码哈希不同（历史遗留），明文同为 123456
-- =====================================================

-- 用户（ID 1-6）
INSERT INTO `user` (`id`,`username`,`password`,`nickname`,`real_name`,`role`,`college`,`phone`,`email`,`avatar`,`last_team_dissolve_time`,`create_time`,`update_time`,`is_deleted`) VALUES
(1,'admin',  '$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu','超级管理员','管理员','ADMIN',  'XX大学',            '13800000000',NULL,           '/images/avatars/avatar1.png',NULL,                     '2026-06-18 11:37:50','2026-06-18 22:54:05',0),
(2,'teacher1','$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu','张老师',    '张老师','TEACHER','XX大学',            '13800000001',NULL,           '/images/avatars/avatar2.png',NULL,                     '2026-06-18 11:37:50','2026-06-18 22:54:05',0),
(3,'teacher2','$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu','李老师',    '李老师','TEACHER','XX大学',            '13800000002',NULL,           '/images/avatars/avatar3.png',NULL,                     '2026-06-18 11:37:50','2026-06-18 22:54:05',0),
(4,'stu1',    '$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu','小明',      '王小明','STUDENT','计算机科学与技术学院','13800000003','123456789@qq','/images/avatars/avatar4.png','2026-06-18 18:20:30',     '2026-06-18 11:37:50','2026-06-18 22:54:05',0),
(5,'stu2',    '$2a$10$NmUW/R86DjeEgZ082dK26uKZT4uETIUdEyite4oP/cDGnQO3IAkDe','小红',      '李小红','STUDENT','XX大学',            '13800000004',NULL,           '/images/avatars/avatar5.png',NULL,                     '2026-06-18 11:37:50','2026-06-18 22:54:05',0),
(6,'stu3',    '$2a$10$jJ9OSKq8/yVHgm2INRRY7eP5PC0xG0baMOhswxQSuD3FJEbb.o3eu','小华',      '张小华','STUDENT','XX大学',            '13800000005',NULL,           '/images/avatars/avatar6.png',NULL,                     '2026-06-18 11:37:50','2026-06-18 22:54:05',0);

-- 竞赛（ID 40-60，21条，全部 status=1 上架）
INSERT INTO `competition` (`id`,`title`,`description`,`category`,`type`,`max_team_size`,`min_team_size`,`register_start`,`register_end`,`comp_start`,`comp_end`,`cover`,`attachment`,`publisher_id`,`teacher_id`,`status`,`create_time`,`update_time`,`is_deleted`) VALUES
(40,'全国大学生数学建模竞赛',           '国家级A类学科竞赛',     '数学建模', 1,NULL,NULL,'2026-06-01 00:00:00','2026-07-15 23:59:59','2026-08-01 00:00:00','2026-08-05 23:59:59','/images/covers/cover1.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 22:54:05',0),
(41,'美国大学生数学建模竞赛',           '国际级数学建模竞赛',     '数学建模', 2,3,3,    '2026-06-22 00:00:00','2026-07-25 23:59:59','2026-08-15 00:00:00','2026-08-20 23:59:59','/images/covers/cover1.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 22:54:05',0),
(42,'全国大学生数学竞赛',               '数学方向竞赛',           '数学建模', 1,NULL,NULL,'2026-05-01 00:00:00','2026-06-10 23:59:59','2026-07-01 00:00:00','2026-07-03 23:59:59','/images/covers/cover6.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(43,'蓝桥杯全国软件和信息技术专业人才大赛','B类学科竞赛',           '程序设计', 1,NULL,NULL,'2026-06-01 00:00:00','2026-07-20 23:59:59','2026-08-10 00:00:00','2026-08-10 23:59:59','/images/covers/cover5.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(44,'ACM程序设计竞赛',                  '算法竞赛',               '程序设计', 2,3,3,    '2026-06-25 00:00:00','2026-07-30 23:59:59','2026-08-20 00:00:00','2026-08-22 23:59:59','/images/covers/cover3.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 22:54:05',0),
(45,'中国大学生程序设计竞赛',           'CCPC竞赛',               '程序设计', 2,3,3,    '2026-04-15 00:00:00','2026-05-20 23:59:59','2026-06-15 00:00:00','2026-06-17 23:59:59','/images/covers/cover7.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(46,'全国大学生电子设计竞赛',           'A类学科竞赛',            '电子设计', 2,3,3,    '2026-06-01 00:00:00','2026-07-10 23:59:59','2026-07-20 00:00:00','2026-07-25 23:59:59','/images/covers/cover8.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(47,'嵌入式系统设计竞赛',               '嵌入式方向',             '电子设计', 1,NULL,NULL,'2026-06-20 00:00:00','2026-07-25 23:59:59','2026-08-10 00:00:00','2026-08-12 23:59:59','/images/covers/cover9.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(48,'RoboMaster机甲大师赛',             '机器人竞赛',             '电子设计', 2,8,3,    '2026-04-01 00:00:00','2026-05-31 23:59:59','2026-07-15 00:00:00','2026-07-20 23:59:59','/images/covers/cover10.png', NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(49,'挑战杯全国大学生课外学术科技作品竞赛','大学生学术科技作品竞赛','挑战杯',   2,6,2,    '2026-06-01 00:00:00','2026-06-30 23:59:59','2026-07-15 00:00:00','2026-10-31 23:59:59','/images/covers/cover5.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(50,'挑战杯中国大学生创业计划竞赛',     '创业计划竞赛',           '挑战杯',   2,5,3,    '2026-06-23 00:00:00','2026-08-15 23:59:59','2026-09-01 00:00:00','2026-11-30 23:59:59','/images/covers/cover2.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(51,'挑战杯红色专项活动',               '红色专项',               '挑战杯',   2,5,3,    '2026-03-01 00:00:00','2026-04-30 23:59:59','2026-06-01 00:00:00','2026-06-10 23:59:59','/images/covers/cover10.png', NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(52,'互联网+大学生创新创业大赛',         'A类创新创业大赛',        '互联网+',  2,5,3,    '2026-06-01 00:00:00','2026-08-15 23:59:59','2026-09-01 00:00:00','2026-11-30 23:59:59','/images/covers/cover2.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 22:54:05',0),
(53,'互联网+产业命题赛道',               '产业命题专项',           '互联网+',  2,5,3,    '2026-06-21 00:00:00','2026-07-31 23:59:59','2026-08-15 00:00:00','2026-09-30 23:59:59','/images/covers/cover2.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 22:54:05',0),
(54,'互联网+红色青年之旅大赛',           '红色旅游赛事',           '互联网+',  2,5,3,    '2026-04-15 00:00:00','2026-05-20 23:59:59','2026-07-01 00:00:00','2026-09-30 23:59:59','/images/covers/cover2.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 22:54:05',0),
(55,'全国大学生机械创新设计大赛',         'A类机械赛事',            '机械设计', 2,5,2,    '2026-06-01 00:00:00','2026-07-20 23:59:59','2026-08-10 00:00:00','2026-08-15 23:59:59','/images/covers/cover8.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(56,'三维数字化创新设计大赛',             '3D产品设计',             '机械设计', 1,NULL,NULL,'2026-06-24 00:00:00','2026-07-25 23:59:59','2026-08-15 00:00:00','2026-08-18 23:59:59','/images/covers/cover9.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(57,'全国大学生工程训练综合能力竞赛',     '工程训练类赛事',         '机械设计', 2,4,2,    '2026-05-01 00:00:00','2026-06-05 23:59:59','2026-07-15 00:00:00','2026-07-20 23:59:59','/images/covers/cover8.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(58,'全国大学生英语竞赛',                 '国家级英语能力竞赛',     '英语竞赛', 1,NULL,NULL,'2026-06-01 00:00:00','2026-07-10 23:59:59','2026-08-01 00:00:00','2026-08-03 23:59:59','/images/covers/cover4.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0),
(59,'外研社全国英语演讲大赛',             '英语演讲赛事',           '英语竞赛', 1,NULL,NULL,'2026-06-22 00:00:00','2026-07-20 23:59:59','2026-08-10 00:00:00','2026-08-12 23:59:59','/images/covers/cover4.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 22:54:05',0),
(60,'全国大学生英语辩论赛',               '英语辩论赛事',           '英语竞赛', 2,4,4,    '2026-05-10 00:00:00','2026-06-05 23:59:59','2026-07-01 00:00:00','2026-07-05 23:59:59','/images/covers/cover4.png',  NULL,1,NULL,1,'2026-06-18 19:52:45','2026-06-18 23:42:30',0);

-- 轮播图（ID 8-12，指向竞赛 40-44）
INSERT INTO `banner` (`id`,`title`,`image_url`,`link_url`,`sort`,`status`,`start_time`,`end_time`,`create_time`,`is_deleted`) VALUES
(8, '全国大学生数学建模竞赛火热报名中',   '/images/banners/banner1.png','/competitions/40',1,1,NULL,NULL,'2026-06-18 22:41:41',0),
(9, '美国大学生数学建模竞赛火热报名中',   '/images/banners/banner1.png','/competitions/41',2,1,NULL,NULL,'2026-06-18 22:41:41',0),
(10,'全国大学生数学竞赛火热报名中',       '/images/banners/banner2.png','/competitions/42',3,1,NULL,NULL,'2026-06-18 22:41:41',0),
(11,'蓝桥杯全国软件和信息技术专业人才大赛火热报名中','/images/banners/banner4.png','/competitions/43',4,1,NULL,NULL,'2026-06-18 22:41:41',0),
(12,'ACM程序设计竞赛火热报名中',          '/images/banners/banner3.png','/competitions/44',5,1,NULL,NULL,'2026-06-18 22:41:41',0);

-- 热门推荐（ID 5-7，指向真实存在的竞赛 40/41/42）
INSERT INTO `hot_recommend` (`id`,`competition_id`,`sort`,`create_time`) VALUES
(5,41,0,'2026-06-18 22:02:07'),
(6,40,1,'2026-06-18 22:02:07'),
(7,42,2,'2026-06-18 22:02:07');

-- 个人报名（ID 2-3 为有效记录；ID 1 已软删除且指向旧竞赛，已剔除）
INSERT INTO `registration` (`id`,`competition_id`,`user_id`,`status`,`attachment`,`description`,`register_time`,`reviewer_id`,`review_remark`,`review_time`,`is_deleted`) VALUES
(2,58,4,0,NULL,NULL,'2026-06-19 12:48:10',NULL,NULL,NULL,0),
(3,40,5,0,NULL,NULL,'2026-06-19 13:46:56',NULL,NULL,NULL,0);

-- 团队（ID 2-4）
INSERT INTO `team` (`id`,`team_name`,`competition_id`,`captain_id`,`invite_code`,`max_size`,`slogan`,`status`,`dissolved_at`,`create_time`,`update_time`,`is_deleted`) VALUES
(2,'1',     1, 4,'F888DB',3,'1',1,'2026-06-18 19:26:50','2026-06-18 18:35:27','2026-06-18 18:35:27',0),
(3,'1',     1, 4,'9B7BD4',3,NULL,0,'2026-06-18 21:20:51','2026-06-18 19:30:06','2026-06-18 19:30:06',0),
(4,'spqrk',46,4,'7E5C7B',5,NULL,1,NULL,                     '2026-06-18 21:54:28','2026-06-18 21:54:28',0);

-- 团队成员（ID 2,5-9）
INSERT INTO `team_member` (`id`,`team_id`,`user_id`,`role`,`status`,`join_time`,`create_time`) VALUES
(2,2,4,1,1,'2026-06-18 18:35:27','2026-06-18 18:35:27'),
(5,2,5,0,1,'2026-06-18 19:00:24','2026-06-18 19:00:24'),
(6,3,4,1,1,'2026-06-18 19:30:06','2026-06-18 19:30:06'),
(7,4,4,1,1,'2026-06-18 21:54:28','2026-06-18 21:54:28'),
(8,4,5,0,1,'2026-06-18 21:55:17','2026-06-18 21:55:17'),
(9,4,6,0,1,'2026-06-18 23:48:45','2026-06-18 23:48:45');

-- 团队报名（ID 2-3 为有效记录；ID 1 已软删除且 team_id=1 不存在，已剔除）
INSERT INTO `team_registration` (`id`,`competition_id`,`team_id`,`status`,`attachment`,`description`,`register_time`,`reviewer_id`,`review_remark`,`review_time`,`is_deleted`) VALUES
(2,1, 2,0,'考核方案A.docx',NULL,'2026-06-18 19:10:37',NULL,NULL,NULL,0),
(3,46,4,1,'http://localhost:8080/file/attachment/2026-06-18/88a7a52f95c84e7cb99ce0d7ff0a1786.docx',NULL,'2026-06-18 23:49:06',1,'通过','2026-06-19 12:23:58',0);

-- 消息（ID 1-9）
INSERT INTO `message` (`id`,`user_id`,`type`,`title`,`content`,`is_read`,`create_time`) VALUES
(1,1,'REGISTRATION','报名通知','新报名：用户 4 报名了 互联网+创新创业大赛',1,'2026-06-18 17:55:14'),
(2,4,'TEAM',       '团队通知','新成员加入了您的团队',1,'2026-06-18 18:39:43'),
(3,4,'TEAM',       '团队通知','有新成员申请加入您的团队，请及时审核',1,'2026-06-18 18:43:16'),
(4,4,'TEAM',       '团队通知','有新成员申请加入您的团队，请及时审核',1,'2026-06-18 19:00:24'),
(5,4,'TEAM',       '团队通知','有新成员申请加入您的团队，请及时审核',1,'2026-06-18 21:55:17'),
(6,4,'TEAM',       '团队通知','有新成员申请加入您的团队，请及时审核',1,'2026-06-18 23:48:45'),
(7,4,'REGISTRATION','报名通知','团队报名已通过',1,'2026-06-19 12:23:58'),
(8,1,'REGISTRATION','报名通知','新报名：用户 4 报名了 全国大学生英语竞赛',1,'2026-06-19 12:48:10'),
(9,1,'REGISTRATION','报名通知','新报名：李小红 报名了 全国大学生数学建模竞赛',1,'2026-06-19 13:46:56');

-- 团队指导老师邀请（ID 1）
INSERT INTO `team_advisor` (`id`,`team_id`,`teacher_id`,`status`,`invited_by`,`invite_time`,`accept_time`,`reject_time`,`remark`) VALUES
(1,4,2,1,4,'2026-06-18 23:47:22','2026-06-18 23:47:47',NULL,NULL);

-- 个人资料修改申请（ID 1，已通过）
INSERT INTO `profile_change_request` (`id`,`user_id`,`field_name`,`old_value`,`new_value`,`status`,`create_time`,`review_time`,`reviewer_id`,`review_remark`,`is_deleted`) VALUES
(1,4,'email',NULL,'123456789@qq',1,'2026-06-18 21:54:52','2026-06-18 21:56:40',1,NULL,0);
