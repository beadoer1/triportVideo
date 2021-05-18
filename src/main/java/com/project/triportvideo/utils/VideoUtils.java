package com.project.triportvideo.utils;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Component
public class VideoUtils {

    @Value("${ffmpeg.ffmpegPath}")
    private String ffmpegPath;
    @Value("${ffmpeg.ffprobePath}")
    private String ffprobePath;
    @Value("${storage.origin}")
    private String originStorage;
    @Value("${storage.encoded}")
    private String encodedStorage;

    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    @PostConstruct
    public void init() {
        try {
            ffmpeg = new FFmpeg(ffmpegPath);
            ffprobe = new FFprobe(ffprobePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String encodingVideo(String filename) {
        String encodedDirectory = encodedStorage + "/" + filename;
        File dir = new File(encodedDirectory);
        if (!dir.exists()) {
            dir.mkdir();
        }

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(originStorage + "/" + filename + ".mp4")   // 인코딩할 파일 경로 및 파일명
                .overrideOutputFiles(true) // 파일이 존재하는 경우 덮어쓰기

                .addOutput(encodedDirectory + "/" + filename + ".m3u8")   // 저장할 경로와 파일명
                .setVideoCodec("libx264")     // video codec h.264로 설정(hevc 등 지원하지 않는 codec 존재하여 변경 필요)
                .setAudioCodec("aac")         // audio codec acc로 설정
                .setFormat("hls")
                .addExtraArgs("-start_number", "0")   // .st 파일(스트리밍) 시작 번호
                .addExtraArgs("-hls_time", "2")       // .st 파일 분리 단위
                .addExtraArgs("-hls_list_size", "0")
                .addExtraArgs("-force_key_frames", "expr:gte(t,n_forced*1)") // 시간으로 분리 가능하도록 강제
                .disableSubtitle()           // No subtiles
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                .done();

        builder.setVerbosity(FFmpegBuilder.Verbosity.DEBUG);

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        // Run a one-pass encode
        executor.createJob(builder).run();

        return encodedDirectory;
    }

    public void cleanStorage() throws IOException {
        File originDir = new File(originStorage);
        File encodedDir = new File(encodedStorage);

        FileUtils.cleanDirectory(originDir);
        FileUtils.cleanDirectory(encodedDir);
        System.out.println("tmp folder 비움 완료");
    }

}

