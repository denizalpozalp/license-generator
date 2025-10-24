package com.odiplus.licensegenerator;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LicenseGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LicenseGeneratorApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
