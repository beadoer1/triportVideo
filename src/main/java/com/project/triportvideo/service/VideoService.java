package com.project.triportvideo.service;

import com.project.triportvideo.innerAPI.VideoUrlDto;
import com.project.triportvideo.utils.S3Utils;
import com.project.triportvideo.utils.VideoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final S3Utils s3Utils;
    private final VideoUtils videoUtils;

    @Value("${origin.ipAddress}")
    private String originServerIpAddress;

    private static final Queue<VideoUrlDto> wailtingQueue = new LinkedList<>();

    @Async // 비동기 동작을 위해 추가(application 파일에 '@EnableAsync'도 추가해줘야 한다.)
    public void encodeVideo(VideoUrlDto videoUrlDto) throws IOException, InterruptedException {
        if(!wailtingQueue.isEmpty()){
            wailtingQueue.offer(videoUrlDto);
            return;
        }
        wailtingQueue.offer(videoUrlDto);
        while(wailtingQueue.size() > 0) {
            try {
                VideoUrlDto originVideoUrlDto = wailtingQueue.peek(); // peek() : queue에서 삭제 없이 값만 확인
                String originVideoUrl =  originVideoUrlDto.getVideoUrl();
                Long originPostId = originVideoUrlDto.getPostId();

                String filename = s3Utils.getVideo(originVideoUrl);
                String encodedDirectory = videoUtils.encodingVideo(filename);
                String videoUrl = s3Utils.uploadFolder(encodedDirectory);

                updateUrl(new VideoUrlDto(originPostId,videoUrl));
            }catch (Exception e){
                System.out.println(e.getMessage());

            }finally{
                videoUtils.cleanStorage();
                wailtingQueue.poll(); // poll() : queue에서 꺼냄(pop()과 동일)
            }
        }
    }

    public void updateUrl(VideoUrlDto videoUrlDto){
        RestTemplate restTemplate = new RestTemplate();
        String videoResourceUrl = originServerIpAddress + "/api/all/posts/video";

        HttpEntity<VideoUrlDto> request = new HttpEntity<>(videoUrlDto);
        restTemplate.postForObject(videoResourceUrl, request, Object.class);
    }
}
