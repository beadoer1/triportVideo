package com.project.triportvideo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/videos/**") // CORS를 적용할 URL 패턴 정의
                .allowedOrigins("http://13.209.8.146") // 허락할 Origin을 지정 ex) "http://localhost:8080", "http://localhost:8081"
                .allowedMethods("POST") // 허욜할 HTTP method를 지정 ex) "GET","POST"
                .maxAge(3600); // pre-flight 리퀘스트를 캐싱 해두는 시간 지정. seconds 단위

        //Default Setting
        //Allow all origins.
        //Allow "simple" methods GET, HEAD and POST.
        //Allow all headers.
        //Set max age to 1800 seconds (30 minutes).
    }
}
