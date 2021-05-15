package com.project.triportvideo.utils;

import com.project.triportvideo.storage.StorageProperties;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.Fraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private final Path rootLocation;
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    @Autowired
    public VideoUtils(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

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
                .setInput(originStorage + "/" + filename + ".mp4")   // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists

                .addOutput(encodedDirectory + "/" + filename + ".m3u8")   // Filename for the destination
                .addExtraArgs("-start_number", "0", "-hls_time", "1", "-hls_list_size", "0")
                .setFormat("hls")
                .disableSubtitle()           // No subtiles
                .setVideoCodec("libx264")     // Video using x264
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                .done();

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

