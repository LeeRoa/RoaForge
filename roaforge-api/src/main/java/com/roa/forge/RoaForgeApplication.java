package com.roa.forge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RoaForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoaForgeApplication.class, args);
    }

}
