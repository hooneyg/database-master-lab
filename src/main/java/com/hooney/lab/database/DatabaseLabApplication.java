package com.hooney.lab.database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 🚀 Database Master Lab Application
 * 
 * @EnableJpaAuditing: 엔티티의 생성/수정 시간을 자동으로 관리하기 위해 활성화
 */
@EnableJpaAuditing
@SpringBootApplication
public class DatabaseLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatabaseLabApplication.class, args);
    }
}
