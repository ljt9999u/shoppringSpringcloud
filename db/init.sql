-- ============================================================
-- 购物商城 + AI 商品查询 数据库初始化脚本
-- 数据库：shopping_mall
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `shopping_mall` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `shopping_mall`;

-- ============================================================
-- 1. 商品表（基于现有 Product POJO 扩展）
-- ============================================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `subtitle` varchar(255) DEFAULT NULL COMMENT '副标题',
  `category` varchar(50) DEFAULT NULL COMMENT '分类(如:手机/衣服/食品)',
  `brand` varchar(50) DEFAULT NULL COMMENT '品牌',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `num` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `description` text COMMENT '商品描述',
  `status` tinyint DEFAULT 1 COMMENT '状态 0下架 1上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================================
-- 2. AI 查询日志表
-- ============================================================
DROP TABLE IF EXISTS `ai_query_log`;
CREATE TABLE `ai_query_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '用户ID(游客为空)',
  `question` varchar(500) NOT NULL COMMENT '用户问题',
  `keywords` varchar(255) DEFAULT NULL COMMENT 'AI提取的关键词(逗号分隔)',
  `matched_count` int DEFAULT 0 COMMENT '匹配到的商品数量',
  `answer` text COMMENT 'AI回复内容',
  `cost_ms` int DEFAULT NULL COMMENT '总耗时(毫秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI查询日志表';

-- ============================================================
-- 3. 商品测试数据
-- ============================================================
INSERT INTO `product` (`product_name`, `subtitle`, `category`, `brand`, `price`, `num`, `description`) VALUES
('iPhone 15 Pro 256GB', '苹果新款旗舰手机', '手机', 'Apple', 8999.00, 100, '苹果最新款 iPhone，A17 Pro 芯片，钛金属机身'),
('华为 Mate60 Pro', '华为旗舰手机', '手机', '华为', 6999.00, 50, '华为 Mate60 Pro，麒麟芯片，卫星通话'),
('小米14', '小米旗舰手机', '手机', '小米', 3999.00, 80, '小米14，骁龙8 Gen3，徕卡光学镜头'),
('红色连衣裙', '夏季新款女装', '衣服', '优衣库', 299.00, 30, '红色雪纺连衣裙，夏季新款，显瘦版型'),
('黑色西装外套', '商务休闲款', '衣服', '海澜之家', 599.00, 20, '黑色西装外套，商务休闲，修身版型'),
('蓝色牛仔裤', '直筒宽松', '衣服', '李维斯', 459.00, 40, '蓝色直筒牛仔裤，宽松舒适'),
('可口可乐 330ml', '碳酸饮料', '饮料', '可口可乐', 3.50, 200, '经典碳酸饮料，冰镇更佳'),
('康师傅红烧牛肉面', '方便面', '食品', '康师傅', 4.50, 150, '经典红烧牛肉味，方便速食'),
('蒙牛纯牛奶 1L', '纯牛奶', '饮料', '蒙牛', 12.90, 100, '新鲜纯牛奶，营养丰富'),
('戴尔 XPS 13 笔记本电脑', '轻薄办公本', '电脑', '戴尔', 9999.00, 15, '戴尔 XPS 13，13寸轻薄笔记本，Intel i7'),
('联想 ThinkPad X1', '商务办公本', '电脑', '联想', 12999.00, 10, '联想 ThinkPad X1 Carbon，商务办公首选'),
('华为 MateBook 14', '办公本', '电脑', '华为', 6999.00, 25, '华为 MateBook 14，2K 触控屏'),
('AirPods Pro 2', '无线耳机', '配件', 'Apple', 1899.00, 60, '苹果无线降噪耳机'),
('小米手环 8', '智能手环', '配件', '小米', 249.00, 100, '小米手环8，运动健康监测'),
('海尔双门冰箱', '家用冰箱', '家电', '海尔', 2999.00, 10, '海尔双门冰箱，节能静音'),
('美的变频空调', '1.5匹挂机', '家电', '美的', 2199.00, 15, '美的1.5匹变频空调，一级能效');

-- ============================================================
-- 验证
-- ============================================================
SELECT '商品表数据:' AS info, COUNT(*) AS total FROM product;
SELECT * FROM product LIMIT 5;
