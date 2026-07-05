package com.opscontrolplane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpsControlPlaneApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsControlPlaneApplication.class, args);
    }
}
