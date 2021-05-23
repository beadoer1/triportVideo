package com.project.triportvideo.service;

import com.project.triportvideo.dto.VideoNameDto;
import com.project.triportvideo.dto.VideoUrlDto;
import com.project.triportvideo.utils.S3Utils;
import com.project.triportvideo.utils.VideoUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Value("${origin.requestURI}")
    private String originServerRequestURI;

    private Logger logger = LoggerFactory.getLogger(this.getClass());



    private static Queue<VideoUrlDto> imposPlayQueue = new LinkedList<>();
    private static Queue<VideoUrlDto> posPlayQueue = new LinkedList<>();

    @Async // 비동기 동작을 위해 추가(application 파일에 '@EnableAsync'도 추가해줘야 한다.)
    public void encodeVideo(VideoUrlDto videoUrlDto) throws IOException {
        if(!imposPlayQueue.isEmpty() || !posPlayQueue.isEmpty()){
            addQueue(videoUrlDto);
            return;
        }
        addQueue(videoUrlDto);

        while(imposPlayQueue.size() > 0 || posPlayQueue.size() > 0) {
            if(imposPlayQueue.size() > 0){
                encoding(imposPlayQueue);
            } else{
                encoding(posPlayQueue);
            }
        }
    }

    public void addQueue(VideoUrlDto videoUrlDto){
        if(!videoUrlDto.getPosPlay()){
            imposPlayQueue.offer(videoUrlDto);
        }else{
            posPlayQueue.offer(videoUrlDto);
        }
    }

    public void encoding(Queue<VideoUrlDto> waitingQueue) throws IOException {
        try {
            VideoUrlDto requestVideoUrlDto = waitingQueue.peek(); // peek() : queue에서 삭제 없이 값만 확인
            String originVideoUrl =  requestVideoUrlDto.getVideoUrl();
            Long originPostId = requestVideoUrlDto.getPostId();
            VideoNameDto videoNameDto = new VideoNameDto(originVideoUrl);

            if(s3Utils.checkFileExist(videoNameDto)) {
                s3Utils.getVideo(videoNameDto);
                videoUtils.encodingVideo(videoNameDto);
                String videoUrl = s3Utils.uploadFolder(videoNameDto);
                updateUrl(new VideoUrlDto(originPostId, videoUrl));
            }
        }catch (Exception e){
            logger.error(e.getMessage());

        }finally{
            videoUtils.cleanStorage();
            waitingQueue.poll(); // poll() : queue에서 꺼냄(pop()과 동일)
        }
    }

    public void updateUrl(VideoUrlDto videoUrlDto){
        RestTemplate restTemplate = new RestTemplate();
        String videoResourceUrl = originServerRequestURI;

        HttpEntity<VideoUrlDto> request = new HttpEntity<>(videoUrlDto);
        restTemplate.postForObject(videoResourceUrl, request, Object.class);
    }
}
