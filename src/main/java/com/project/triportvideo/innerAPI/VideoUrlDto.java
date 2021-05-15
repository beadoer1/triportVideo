package com.project.triportvideo.innerAPI;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class VideoUrlDto implements Serializable {
    private Long postId;
    private String videoUrl;

    public VideoUrlDto(Long postId, String videoUrl){
        this.postId = postId;
        this.videoUrl = videoUrl;
    }
}

