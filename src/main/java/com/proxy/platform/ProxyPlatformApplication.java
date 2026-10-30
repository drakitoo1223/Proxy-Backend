package com.proxy.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProxyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyPlatformApplication.class, args);
    }
}
