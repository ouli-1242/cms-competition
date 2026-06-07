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
  `school` VARCHAR(100),
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

-- =====================================================
-- 测试数据（密码统一为 123456）
-- BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- =====================================================
INSERT INTO `user`(username,password,nickname,real_name,role,school,phone) VALUES
('admin',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', '管理员',  'ADMIN',   'XX大学', '13800000000'),
('teacher1','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张老师',     '张老师',  'TEACHER', 'XX大学', '13800000001'),
('teacher2','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李老师',     '李老师',  'TEACHER', 'XX大学', '13800000002'),
('stu1',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '小明',       '王小明',  'STUDENT', 'XX大学', '13800000003'),
('stu2',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '小红',       '李小红',  'STUDENT', 'XX大学', '13800000004'),
('stu3',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '小华',       '张小华',  'STUDENT', 'XX大学', '13800000005');

INSERT INTO `competition`(title,description,category,type,max_team_size,min_team_size,register_start,register_end,publisher_id,teacher_id,status) VALUES
('全国大学生数学建模竞赛', '全国性数学建模赛事，欢迎组队参加', '学科竞赛', 2, 3, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 1, 2, 1),
('互联网+创新创业大赛',     '创新创业大赛，个人赛',          '创新创业', 1, 1, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 1, 3, 1),
('ACM 程序设计竞赛',        '编程类团队赛，每队 3 人',      '学科竞赛', 2, 3, 2, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 1, 2, 1),
('英语演讲比赛',            '个人赛，提升英语表达',          '文体艺术', 1, 1, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 1, 2, 0);

INSERT INTO `banner`(title,image_url,link_url,sort,status) VALUES
('数学建模竞赛火热报名中', 'https://placehold.co/1200x400/4A90E2/fff?text=Math+Modeling', '/competition/1', 1, 1),
('互联网+大赛启动',         'https://placehold.co/1200x400/67C23A/fff?text=Internet+',    '/competition/2', 2, 1);

INSERT INTO `hot_recommend`(competition_id,sort) VALUES
(1, 100),
(3, 90);
