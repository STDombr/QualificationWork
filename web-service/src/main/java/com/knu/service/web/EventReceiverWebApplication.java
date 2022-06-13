package com.knu.service.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class EventReceiverWebApplication extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
        SpringApplication.run(EventReceiverWebApplication.class, args);
    }
}
