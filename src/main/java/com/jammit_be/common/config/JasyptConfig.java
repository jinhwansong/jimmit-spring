package com.jammit_be.common.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class JasyptConfig {

    private final Environment environment;

    public JasyptConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        
        // 1순위: 시스템 프로퍼티
        String password = System.getProperty("jasypt.encryptor.password");
        if (password == null || password.isEmpty()) {
            password = System.getProperty("jasypt_key");
        }
        
        // 2순위: 환경변수 (.env 파일에서 로드된 값 포함)
        if (password == null || password.isEmpty()) {
            password = System.getenv("JASYPT_PASSWORD");
        }
        if (password == null || password.isEmpty()) {
            password = System.getenv("jasypt_key"); // .env 파일의 jasypt_key
        }
        
        // 3순위: Environment에서 읽기 (spring-dotenv가 로드한 .env 값 + application.yml의 ${jasypt_key} 기본값 포함)
        if (password == null || password.isEmpty()) {
            password = environment.getProperty("jasypt.encryptor.key");
        }
        
        // 최종 확인: 여전히 없으면 기본값 사용
        if (password == null || password.isEmpty()) {
            password = "1234"; // application.yml의 기본값과 동일
        }
        
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
}