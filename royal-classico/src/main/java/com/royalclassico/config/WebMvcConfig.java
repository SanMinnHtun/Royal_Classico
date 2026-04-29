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

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);

        // Also expose a friendly /images/** mapping for templates to use
        registry.addResourceHandler("/images/**")
                .addResourceLocations(absolutePath);

        // Serve static classpath resources (CSS, JS, fonts, etc.)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
