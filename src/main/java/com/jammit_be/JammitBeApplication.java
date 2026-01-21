package com.jammit_be;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class JammitBeApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(JammitBeApplication.class);
        var ctx = app.run(args);
        Environment env = ctx.getEnvironment();
        
        // 환경변수 로드 확인 (디버깅용)
        log.info("=== 환경변수 확인 ===");
        log.info("DB_HOST: {}", env.getProperty("DB_HOST", "NOT_SET"));
        log.info("DB_PORT: {}", env.getProperty("DB_PORT", "NOT_SET"));
        log.info("DB_USERNAME: {}", env.getProperty("DB_USERNAME", "NOT_SET"));
        log.info("DB_PASSWORD: {}", env.getProperty("DB_PASSWORD", "NOT_SET") != null ? "***SET***" : "NOT_SET");
        log.info("SUPABASE_URL: {}", env.getProperty("SUPABASE_URL", "NOT_SET"));
        log.info("SUPABASE_SERVICE_ROLE_KEY: {}", env.getProperty("SUPABASE_SERVICE_ROLE_KEY", "NOT_SET") != null ? "***SET***" : "NOT_SET");
        log.info("===================");
    }

}
