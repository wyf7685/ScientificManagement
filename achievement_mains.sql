/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : strapi

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 26/01/2026 15:21:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for achievement_mains
-- ----------------------------
DROP TABLE IF EXISTS `achievement_mains`;
CREATE TABLE `achievement_mains`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `document_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `published_at` datetime(6) NULL DEFAULT NULL,
  `created_by_id` int UNSIGNED NULL DEFAULT NULL,
  `updated_by_id` int UNSIGNED NULL DEFAULT NULL,
  `locale` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `achievement_status` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `summary` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL,
  `is_delete` int NULL DEFAULT NULL,
  `creator_id` int UNSIGNED NULL DEFAULT NULL COMMENT '创建者用户ID（业务用户）',
  `creator_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者姓名',
  `creator_dept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者部门',
  `reviewer_id` int UNSIGNED NULL DEFAULT NULL COMMENT '当前审核人ID',
  `reviewer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前审核人姓名',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '最新审核意见',
  `reviewed_at` datetime(6) NULL DEFAULT NULL COMMENT '最新审核时间',
  `visibility_range` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `year` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `authors` json NULL,
  `keywords` json NULL,
  `project_code` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `project_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `created_by_user_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `achievement_mains_documents_idx`(`document_id` ASC, `locale` ASC, `published_at` ASC) USING BTREE,
  INDEX `achievement_mains_created_by_id_fk`(`created_by_id` ASC) USING BTREE,
  INDEX `achievement_mains_updated_by_id_fk`(`updated_by_id` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_reviewer_id`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_achievement_status`(`achievement_status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_reviewed_at`(`reviewed_at` ASC) USING BTREE,
  CONSTRAINT `achievement_mains_created_by_id_fk` FOREIGN KEY (`created_by_id`) REFERENCES `admin_users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `achievement_mains_updated_by_id_fk` FOREIGN KEY (`updated_by_id`) REFERENCES `admin_users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of achievement_mains
-- ----------------------------
INSERT INTO `achievement_mains` VALUES (1, 'kashrvfxl0ohti6xt3cp96ug', '2025-12-04 10:34:08.634000', '2025-12-04 10:34:08.634000', NULL, 1, 1, NULL, '深度学习研究论文', 'PENDING', '......................', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `achievement_mains` VALUES (2, 'kashrvfxl0ohti6xt3cp96ug', '2025-12-04 10:34:08.634000', '2025-12-04 10:34:08.634000', '2025-12-04 10:34:08.657000', 1, 1, NULL, '深度学习研究论文', 'PENDING', '......................', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `achievement_mains` VALUES (3, 'opxiij4215mf75921u2dm51f', '2026-01-23 13:44:48.861000', '2026-01-24 23:02:04.145114', NULL, 1, NULL, NULL, '你好', 'APPROVED', '', 0, NULL, NULL, NULL, 2, '李四', '测试审核意见1', '2026-01-24 23:02:04.145114', 'internal_abstract', '2026', '[\"赵六\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (4, 'opxiij4215mf75921u2dm51f', '2026-01-23 13:44:48.861000', '2026-01-24 23:02:04.145114', '2026-01-23 13:44:48.887000', 1, NULL, NULL, '你好', 'APPROVED', '', 0, NULL, NULL, NULL, 2, '李四', '测试审核意见1', '2026-01-24 23:02:04.145114', 'internal_abstract', '2026', '[\"赵六\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (5, 'o22djouldwpsvipvexg7ftxx', '2026-01-24 22:44:54.330000', '2026-01-24 23:02:26.557990', NULL, 1, NULL, NULL, '测试论文1', 'REJECTED', '测试1', 0, NULL, NULL, NULL, 2, '李四', '测试审核意见2', '2026-01-24 23:02:26.557990', 'public_abstract', '2020', '[\"张三\"]', '[\"1\"]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (6, 'o22djouldwpsvipvexg7ftxx', '2026-01-24 22:44:54.330000', '2026-01-24 23:02:26.557990', '2026-01-24 22:44:54.349000', 1, NULL, NULL, '测试论文1', 'REJECTED', '测试1', 0, NULL, NULL, NULL, 2, '李四', '测试审核意见2', '2026-01-24 23:02:26.557990', 'public_abstract', '2020', '[\"张三\"]', '[\"1\"]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (7, 'v6auvuouaqa44eqlt75w0vl3', '2026-01-24 22:59:38.290000', '2026-01-24 23:02:34.467706', NULL, 1, NULL, NULL, '测试专利2', 'APPROVED', '测试专利摘要2', 0, NULL, NULL, NULL, 2, '李四', '测试审核意见3', '2026-01-24 23:02:34.467706', 'public_abstract', '2020', '[\"张三\"]', '[\"测试2\"]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (8, 'v6auvuouaqa44eqlt75w0vl3', '2026-01-24 22:59:38.290000', '2026-01-24 23:02:34.467706', '2026-01-24 22:59:38.317000', 1, NULL, NULL, '测试专利2', 'APPROVED', '测试专利摘要2', 0, NULL, NULL, NULL, 2, '李四', '测试审核意见3', '2026-01-24 23:02:34.467706', 'public_abstract', '2020', '[\"张三\"]', '[\"测试2\"]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (9, 'j3dzvdhhpwt679rv7bjqsoay', '2026-01-24 23:29:26.455000', '2026-01-25 11:32:49.451607', NULL, 1, NULL, NULL, '测试软著1', 'REJECTED', '1', 0, NULL, NULL, NULL, 2, '李四', '不通过', '2026-01-25 11:32:49.451607', 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (10, 'j3dzvdhhpwt679rv7bjqsoay', '2026-01-24 23:29:26.455000', '2026-01-25 11:32:49.451607', '2026-01-24 23:29:26.485000', 1, NULL, NULL, '测试软著1', 'REJECTED', '1', 0, NULL, NULL, NULL, 2, '李四', '不通过', '2026-01-25 11:32:49.451607', 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (11, 'mtdv93itlhbc6nxpv34u16zk', '2026-01-24 23:32:14.809000', '2026-01-25 11:35:44.697399', NULL, 1, NULL, NULL, '测试软著2', 'APPROVED', '1', 0, NULL, NULL, NULL, 2, '李四', '审核通过', '2026-01-25 11:35:44.697399', 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (12, 'mtdv93itlhbc6nxpv34u16zk', '2026-01-24 23:32:14.809000', '2026-01-25 11:35:44.697399', '2026-01-24 23:32:14.819000', 1, NULL, NULL, '测试软著2', 'APPROVED', '1', 0, NULL, NULL, NULL, 2, '李四', '审核通过', '2026-01-25 11:35:44.697399', 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (20, 'ahxl48skmrfviwtjn28t4ioe', '2026-01-25 00:16:45.873000', '2026-01-25 11:32:16.742336', NULL, NULL, NULL, NULL, '1', 'UNDER_REVIEW', '', 0, NULL, NULL, NULL, 2, '李四', NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (21, 'ahxl48skmrfviwtjn28t4ioe', '2026-01-25 00:16:45.873000', '2026-01-25 11:32:16.742336', '2026-01-25 00:16:45.886000', NULL, NULL, NULL, '1', 'UNDER_REVIEW', '', 0, NULL, NULL, NULL, 2, '李四', NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', NULL);
INSERT INTO `achievement_mains` VALUES (22, 'etods7u8oe8q7cdk8tsuqqxx', '2026-01-25 00:28:05.746000', '2026-01-25 11:32:10.358256', NULL, NULL, NULL, NULL, '2', 'UNDER_REVIEW', '', 0, NULL, NULL, NULL, 2, '李四', NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', '1');
INSERT INTO `achievement_mains` VALUES (23, 'etods7u8oe8q7cdk8tsuqqxx', '2026-01-25 00:28:05.746000', '2026-01-25 11:32:10.358256', '2026-01-25 00:28:05.777000', NULL, NULL, NULL, '2', 'UNDER_REVIEW', '', 0, NULL, NULL, NULL, 2, '李四', NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', '1');
INSERT INTO `achievement_mains` VALUES (24, 'jhfidqwrxpzptgngwzoxff7p', '2026-01-25 11:36:54.672000', '2026-01-25 11:37:17.866242', NULL, NULL, NULL, NULL, '测试专利3-提交附件', 'UNDER_REVIEW', '测试专利3', 0, NULL, NULL, NULL, 2, '李四', NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[\"无\"]', '', '', '1');
INSERT INTO `achievement_mains` VALUES (25, 'jhfidqwrxpzptgngwzoxff7p', '2026-01-25 11:36:54.672000', '2026-01-25 11:37:17.866242', '2026-01-25 11:36:54.697000', NULL, NULL, NULL, '测试专利3-提交附件', 'UNDER_REVIEW', '测试专利3', 0, NULL, NULL, NULL, 2, '李四', NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[\"无\"]', '', '', '1');
INSERT INTO `achievement_mains` VALUES (26, 'yoep024fpv1bbpsqmgb4yp3a', '2026-01-26 09:56:12.373000', '2026-01-26 09:56:12.373000', NULL, NULL, NULL, NULL, '1', 'PENDING', '', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', '1');
INSERT INTO `achievement_mains` VALUES (27, 'yoep024fpv1bbpsqmgb4yp3a', '2026-01-26 09:56:12.373000', '2026-01-26 09:56:12.373000', '2026-01-26 09:56:12.400000', NULL, NULL, NULL, '1', 'PENDING', '', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'public_abstract', '2026', '[\"张三\"]', '[]', '', '', '1');

SET FOREIGN_KEY_CHECKS = 1;
