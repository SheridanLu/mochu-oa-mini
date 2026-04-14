package com.mochu.oa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mochu.oa.mapper")
public class MochuOaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MochuOaApplication.class, args);
    }
}