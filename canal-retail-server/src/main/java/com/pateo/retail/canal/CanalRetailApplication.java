package com.pateo.retail.canal;

import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableNacosConfig
public class CanalRetailApplication {

    public static void main(String[] args) {

        SpringApplication.run(CanalRetailApplication.class, args);
    }



}
