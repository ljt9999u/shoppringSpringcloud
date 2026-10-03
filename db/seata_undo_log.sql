-- ============================================================
-- Seata AT 模式 undo_log 回滚日志表
-- 作用：AT 模式下每个【参与全局事务的业务库】都必须有这张表，
--       RM 执行分支事务时保存前后镜像，全局回滚时据此自动补偿。
-- 适用：Seata 2.x（本项目由 spring-cloud-alibaba 2023.0.3.2 管理版本）
-- 注意：应用连接的库名是 shopping（不是 init.sql 里的 shopping_mall）
-- ============================================================

CREATE DATABASE IF NOT EXISTS `shopping` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `shopping`;

CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id`     BIGINT       NOT NULL COMMENT '分支事务ID',
  `xid`           VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context`       VARCHAR(128) NOT NULL COMMENT '上下文（如序列化方式）',
  `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚镜像数据',
  `log_status`    INT          NOT NULL COMMENT '状态：0正常 1全局回滚中',
  `log_created`   DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified`  DATETIME(6)  NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Seata AT 模式回滚日志表';
