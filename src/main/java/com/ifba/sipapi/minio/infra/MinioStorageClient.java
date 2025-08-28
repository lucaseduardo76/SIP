package com.ifba.sipapi.minio.infra;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.item.Item;
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
import java.util.Random;
import java.util.UUID;

@Component
@Log4j2
@RequiredArgsConstructor
public class MinioStorageClient implements MinioClient {

    private final S3Client s3Client;

    @Value("${minio.bucketItems}")
    private String itemsBucket;

    @Value("${minio.bucketProfile}")
    private String profileBucket;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Override
    public String uploadItemsImage(MultipartFile profileImage, Item item) {
        log.info("[start] MinioStorageClient - uploadItemsImage");
        ensureBucketExists(itemsBucket);

        String prefix = item.getCode() + "_" + UUID.randomUUID() + "_item";
        String filename = generateProfileImageFilename(prefix, profileImage);
        uploadFileToBucket(profileImage, filename, itemsBucket);
        log.debug("[finish] MinioStorageClient - uploadItemsImage");
        return buildPublicImageUrl(filename, itemsBucket);
    }

    @Override
    public String uploadUserProfileImage(MultipartFile profileImage, User user) {
        log.info("[start] minioApplicationService - updateProfileImage");
        ensureBucketExists(profileBucket);

        String prefix = user.getId() + "_profile";
        String filename = generateProfileImageFilename(prefix, profileImage);
        uploadFileToBucket(profileImage, filename, profileBucket);
        log.debug("[finish] minioApplicationService - updateProfileImage");
        return buildPublicImageUrl(filename, profileBucket);
    }

    private void ensureBucketExists(String bucketName) {
        boolean exists = s3Client.listBuckets().buckets().stream().anyMatch(b -> b.name().equals(bucketName));

        if (!exists) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    private String generateProfileImageFilename(String prefix, MultipartFile file) {
        String extension = extractExtension(file);
        return prefix + extension;
    }

    private void uploadFileToBucket(MultipartFile file, String filename, String bucketName) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
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

    private String buildPublicImageUrl(String filename, String buket) {
        return String.format("%s/%s/%s", minioEndpoint, buket, filename);
    }
}
