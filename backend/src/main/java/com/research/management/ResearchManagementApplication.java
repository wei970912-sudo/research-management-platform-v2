package com.research.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ResearchManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchManagementApplication.class, args);
    }
}
