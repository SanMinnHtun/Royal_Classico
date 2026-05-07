package com.royalclassico.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

/**
 * MVC configuration: serves uploaded images as static resources
 * under the /uploads/** and /images/** URL patterns from the local filesystem.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();

        // Serve images packaged in the classpath first (src/main/resources/static/images)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);

        // Expose uploads (user-generated content) under /uploads/** mapping
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(3600);

        // For backwards compatibility, also allow a fallback /images/** mapping to the uploads folder
        registry.addResourceHandler("/images/uploads/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600);

        // Serve static classpath resources (CSS, JS, fonts, etc.) under /static/**
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}
