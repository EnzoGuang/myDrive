package com.yg.mydrive;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yg.mydrive.mapper")
public class MydriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(MydriveApplication.class, args);
    }

}
