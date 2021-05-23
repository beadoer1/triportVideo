package com.project.triportvideo.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.project.triportvideo.dto.VideoNameDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

@Component
public class S3Utils {
    private AmazonS3 s3Client;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomainName;
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;
    @Value("${storage.origin}")
    private String originStorage;
    @Value("${storage.encoded}")
    private String encodedStorage;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public void getVideo(VideoNameDto videoNameDto) throws IOException {
        logger.info("Downloading {} from S3 bucket {}...\n", videoNameDto.getFullname(), bucket);
        try {
            // Get an object and print its contents.
            logger.info("Downloading an object");
            String fileS3 = "video/" + videoNameDto.getFilename() + "/" + videoNameDto.getFullname();
            S3Object o = s3Client.getObject(bucket, fileS3);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(originStorage+"/"+videoNameDto.getFullname());
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
        } catch (AmazonServiceException | IOException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
    }

    public String uploadFolder(VideoNameDto videoNameDto) throws AmazonServiceException, InterruptedException {
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        File file = new File(encodedStorage + "/" + videoNameDto.getFilename());
        try {
            transferManager.uploadDirectory(bucket, "video/" + videoNameDto.getFilename(), file, false).waitForCompletion();
            String videoUrl = "https://" + cloudFrontDomainName + "/video/" +videoNameDto.getFilename() + "/" + videoNameDto.getFilename() + ".m3u8";
            logger.info(videoUrl +" S3 저장 완료");
            return videoUrl;
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
            ;
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean checkFileExist(VideoNameDto videoNameDto){
        boolean isExistObject = s3Client.doesObjectExist(bucket, "video/" +videoNameDto.getFilename() + "/" + videoNameDto.getFilename() + "." + videoNameDto.getType());
        return isExistObject;
    }
}
