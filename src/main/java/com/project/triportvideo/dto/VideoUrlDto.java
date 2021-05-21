package com.project.triportvideo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoUrlDto implements Serializable {
    private Long postId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean posPlay;
    private String videoUrl;

    public VideoUrlDto(Long postId, String videoUrl){
        this.postId = postId;
        this.videoUrl = videoUrl;
    }
}

