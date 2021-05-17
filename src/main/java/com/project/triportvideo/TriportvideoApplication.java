package com.project.triportvideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TriportvideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TriportvideoApplication.class, args);
    }

}
