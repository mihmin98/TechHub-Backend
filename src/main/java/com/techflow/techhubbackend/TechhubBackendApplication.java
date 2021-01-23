package com.techflow.techhubbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class TechhubBackendApplication {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public static void main(String[] args) {
        SpringApplication.run(TechhubBackendApplication.class, args);
    }

}
