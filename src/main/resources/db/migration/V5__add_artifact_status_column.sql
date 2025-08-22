-- Add status column to artifact table
ALTER TABLE `artifact` ADD COLUMN `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NOT_FOUND' AFTER `job_id`;
