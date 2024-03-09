package com.shortlink.shardingtest;

/**
 * @author LYT0905
 * @date 2024/02/29/16:23
 */


public class ShardingTest {
    private static final String SQL = "CREATE TABLE `t_link_stats_today_0`\n" +
            "(\n" +
            "    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "    `gid`            varchar(32)  DEFAULT 'default' COMMENT '分组标识',\n" +
            "    `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接',\n" +
            "    `date`           date         DEFAULT NULL COMMENT '日期',\n" +
            "    `today_pv`       int(11) DEFAULT '0' COMMENT '今日PV',\n" +
            "    `today_uv`       int(11) DEFAULT '0' COMMENT '今日UV',\n" +
            "    `today_uip`      int(11) DEFAULT '0' COMMENT '今日IP数',\n" +
            "    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',\n" +
            "    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',\n" +
            "    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
            "    PRIMARY KEY (`id`),\n" +
            "    UNIQUE KEY `idx_unique_today_stats` (`full_short_url`,`gid`,`date`) USING BTREE\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;;";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++){
            System.out.printf((SQL) + "%n", i);
        }
    }
}
