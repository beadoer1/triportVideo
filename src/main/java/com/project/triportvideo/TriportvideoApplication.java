package com.project.triportvideo;

import com.project.triportvideo.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(StorageProperties.class)
public class TriportvideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TriportvideoApplication.class, args);
    }

}
