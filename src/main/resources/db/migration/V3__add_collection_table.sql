CREATE Table IF NOT EXISTS dataset_collection (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(512),
    bids_dataset_id int(10) UNSIGNED ZEROFILL NOT NULL,
    storage_id int(10) UNSIGNED ZEROFILL NOT NULL,
    storage_path varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    collection_execution_id varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    status varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);