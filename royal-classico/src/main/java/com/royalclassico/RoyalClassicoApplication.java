package com.royalclassico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Royal Classico FC — Spring Boot Application Entry Point.
 *
 * @EnableMongoRepositories explicitly activates Spring Data MongoDB
 * repository scanning, pointing at the repository package.
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.royalclassico.repository")
public class RoyalClassicoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoyalClassicoApplication.class, args);
    }
}
