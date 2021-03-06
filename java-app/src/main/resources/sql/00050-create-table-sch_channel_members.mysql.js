db.execute('''
CREATE TABLE IF NOT EXISTS `sch_channel_members` (
`id` bigint(20) unsigned NOT NULL,
    `userid`  bigint(20)  NULL ,
    `channelid`  bigint(20)  NULL ,
    `owner`  bit(1)  NOT NULL  DEFAULT 0 ,
    `joinedat`  datetime  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`),
  UNIQUE KEY `channel_user_key` (`channelid`, `userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');
