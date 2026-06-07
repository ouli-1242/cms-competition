package com.cms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 竞赛管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.cms.mapper")
@EnableAsync
@EnableScheduling
public class CmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
        System.out.println("\n========== CMS Backend Started ==========");
        System.out.println("API:    http://localhost:8080");
        System.out.println("==========================================\n");
    }
}
