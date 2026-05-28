package com.example.camunda_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CamundaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaClientApplication.class, args);
    }
}
