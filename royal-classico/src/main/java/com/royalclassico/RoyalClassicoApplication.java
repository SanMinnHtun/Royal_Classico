package com.royalclassico;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Bean
    public CommandLineRunner ensureUploadDirs(@Value("${app.upload.dir:./uploads}") String uploadsDir) {
        return args -> {
            try {
                Path base = Paths.get(uploadsDir).toAbsolutePath().normalize();
                Path players = base.resolve("players");
                Path news = base.resolve("news");
                Files.createDirectories(players);
                Files.createDirectories(news);
                System.out.println("Ensured upload directories exist: " + players + " , " + news);
            } catch (Exception e) {
                System.err.println("Failed to create upload directories: " + e.getMessage());
            }
        };
    }
}
