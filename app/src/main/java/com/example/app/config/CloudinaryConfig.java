package com.example.app.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dppiuypop",
            "api_key", "412712715735329",
            "api_secret", "m04IUY0-awwtr4YoS-1xvxOOIzU"
        ));
    }
}
