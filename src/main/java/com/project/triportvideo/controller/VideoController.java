package com.project.triportvideo.controller;

import com.project.triportvideo.innerAPI.VideoUrlDto;
import com.project.triportvideo.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/videos/encoding")
    public void encodeVideo(@RequestBody VideoUrlDto videoUrl) throws IOException, InterruptedException {
        videoService.encodeVideo(videoUrl);
    }
}
