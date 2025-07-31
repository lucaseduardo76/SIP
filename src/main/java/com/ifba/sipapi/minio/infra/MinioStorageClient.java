package com.ifba.sipapi.minio.infra;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.minio.application.service.MinioClient;
import com.ifba.sipapi.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Component
@Log4j2
@RequiredArgsConstructor
public class MinioStorageClient implements MinioClient {

    private final S3Client s3Client;

    @Value("${minio.bucketProfile}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Override
    public String uploadUserProfileImage(MultipartFile profileImage, User user) {
        log.info("[start] minioApplicationService - updateProfileImage");
        ensureBucketExists();

        String filename = generateProfileImageFilename(user, profileImage);
        uploadFileToBucket(profileImage, filename);
        log.debug("[finish] minioApplicationService - updateProfileImage");
        return buildPublicImageUrl(filename);
    }

    private void ensureBucketExists() {
        boolean exists = s3Client.listBuckets().buckets().stream().anyMatch(b -> b.name().equals(bucket));

        if (!exists) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    private String generateProfileImageFilename(User user, MultipartFile file) {
        String extension = extractExtension(file);
        return user.getId() + "_profile" + extension;
    }

    private void uploadFileToBucket(MultipartFile file, String filename) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Unable to save the new image. Please try again.");
        }
    }

    private String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "";
    }

    private String buildPublicImageUrl(String filename) {
        return String.format("%s/%s/%s", minioEndpoint, bucket, filename);
    }
}
