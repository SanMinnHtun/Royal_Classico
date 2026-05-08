package com.royalclassico.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files under uploads directory at the /uploads/** URL path
        Path uploadPath = Paths.get(uploadsDir).toAbsolutePath().normalize();
        String externalLocation = uploadPath.toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(externalLocation)
                .setCachePeriod(3600);

        // Ensure static resources under classpath:/static/ still work
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);
    }
}

