package com.poultry.smartfarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class SmartPoultryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPoultryApplication.class, args);
    }
}
