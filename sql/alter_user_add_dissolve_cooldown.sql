-- 团队软解散：在 team 表添加 dissolved_at 字段（12小时内可恢复）
ALTER TABLE `team`
ADD COLUMN `dissolved_at` datetime DEFAULT NULL COMMENT '解散时间，12小时内队长可恢复'
AFTER `status`;
