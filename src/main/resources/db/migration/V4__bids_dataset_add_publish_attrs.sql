ALTER TABLE bids_dataset
ADD COLUMN publish_at TIMESTAMP NULL COMMENT 'The date the dataset was published in source system',
ADD COLUMN created_at TIMESTAMP NULL COMMENT 'The date the dataset was created in source system',
ADD COLUMN updated_at TIMESTAMP NULL COMMENT 'The date the dataset was last updated in source system';