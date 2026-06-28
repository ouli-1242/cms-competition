# CMS Competition — 项目记忆

## 技术栈
- 后端: Spring Boot + MyBatis-Plus + MySQL
- 前端: Vue 3
- 数据库: MySQL, 通过 sql/init.sql 初始化

## 注意事项
- MyBatis-Plus 字段映射规则: camelCase → snake_case（如 `college` → `college`）
- SQL 脚本中的列名必须与实体类字段名一致（驼峰），否则映射失败
- 密码使用 BCrypt 加密
