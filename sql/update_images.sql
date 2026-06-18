-- 更新现有数据库的图片路径（在 Navicat 中执行）

-- 用户头像
UPDATE `user` SET avatar = '/images/avatars/avatar1.png' WHERE username = 'admin';
UPDATE `user` SET avatar = '/images/avatars/avatar2.png' WHERE username = 'teacher1';
UPDATE `user` SET avatar = '/images/avatars/avatar3.png' WHERE username = 'teacher2';
UPDATE `user` SET avatar = '/images/avatars/avatar4.png' WHERE username = 'stu1';
UPDATE `user` SET avatar = '/images/avatars/avatar5.png' WHERE username = 'stu2';
UPDATE `user` SET avatar = '/images/avatars/avatar6.png' WHERE username = 'stu3';

-- 竞赛封面
UPDATE `competition` SET cover = '/images/covers/cover1.png' WHERE title = '全国大学生数学建模竞赛';
UPDATE `competition` SET cover = '/images/covers/cover2.png' WHERE title = '互联网+创新创业大赛';
UPDATE `competition` SET cover = '/images/covers/cover3.png' WHERE title = 'ACM 程序设计竞赛';
UPDATE `competition` SET cover = '/images/covers/cover4.png' WHERE title = '英语演讲比赛';

-- 轮播图
UPDATE `banner` SET image_url = '/images/banners/banner1.png' WHERE title = '数学建模竞赛火热报名中';
UPDATE `banner` SET image_url = '/images/banners/banner2.png' WHERE title = '互联网+大赛启动';
UPDATE `banner` SET image_url = '/images/banners/banner3.png' WHERE title = 'ACM程序设计竞赛';
UPDATE `banner` SET image_url = '/images/banners/banner4.png' WHERE title = '英语演讲比赛';
UPDATE `banner` SET image_url = '/images/banners/banner5.png' WHERE title = '创新创业大赛等你来';
