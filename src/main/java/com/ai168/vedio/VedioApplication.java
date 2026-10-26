package com.ai168.vedio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.ai168.vedio.mapper")
public class VedioApplication {
    public static void main(String[] args) {
        SpringApplication.run(VedioApplication.class, args);
    }
}
