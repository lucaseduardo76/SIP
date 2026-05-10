package com.ifba.sipapi.minio.infra;

import com.ifba.sipapi.config.handler.APIException;
import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.minio.api.service.MinioClient;
import com.ifba.sipapi.minio.dto.BucketFileDto;
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
import java.net.URI;
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

    @Override
    public String uploadItemsImage(MultipartFile itemImage, Item item) {
        log.info("[start] MinioStorageClient - uploadItemsImage");
        try {
            ensureBucketExists(itemsBucket);
            validateIsImage(itemImage);
            String prefix = item.getCode() + "_" + UUID.randomUUID() + "_item";
            String filename = generateProfileImageFilename(prefix, itemImage);
            uploadFileToBucket(itemImage, filename, itemsBucket);
            log.debug("[finish] MinioStorageClient - uploadItemsImage");
            return buildPublicImageUrl(filename, itemsBucket);
        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Erro ao tentar criar item, imagem inconsistente, tente novamente ou troque a imagem");
        }
    }

    @Override
    public String uploadUserProfileImage(MultipartFile profileImage, User user) {
        log.info("[start] minioApplicationService - updateProfileImage");
        ensureBucketExists(profileBucket);
        validateIsImage(profileImage);

        String prefix = user.getId() + "_profile";
        String filename = generateProfileImageFilename(prefix, profileImage);
        uploadFileToBucket(profileImage, filename, profileBucket);
        log.debug("[finish] minioApplicationService - updateProfileImage");
        return buildPublicImageUrl(filename, profileBucket);
    }

    @Override
    public void deleteItemImage(String imageUrl) {
        log.info("[start] MinioStorageClient - deleteItemImage");
        try {
            BucketFileDto bucketFileDto = extractBucketAndFilenameFromUrl(imageUrl);
            s3Client.deleteObject(builder -> builder.bucket(bucketFileDto.getBucket()).key(bucketFileDto.getFilename()));
            log.debug("[finish] MinioStorageClient - deleteItemImage");
        } catch (Exception e) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Não foi possível excluir a imagem do item: " + imageUrl);
        }
    }

    private BucketFileDto extractBucketAndFilenameFromUrl(String imageUrl) {
        String path = imageUrl.contains("://") ? URI.create(imageUrl).getPath() : imageUrl;
        if (path.startsWith("/"))
            path = path.substring(1);

        String[] parts = path.split("/", 2);
        if (parts.length < 2)
            throw APIException.build(HttpStatus.BAD_REQUEST, "URL de imagem inválida: " + imageUrl);

        return new BucketFileDto(parts[0], parts[1]);
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

    private String buildPublicImageUrl(String filename, String bucket) {
        return String.format("/%s/%s", bucket, filename);
    }

    private void validateIsImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Somente arquivos de imagem são permitidos.");
        }

        validateExtension(file);
    }

    private void validateExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            throw APIException.build(HttpStatus.BAD_REQUEST, "Arquivo inválido.");

        String lowerName = originalFilename.toLowerCase();
        if (!(lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")))
            throw APIException.build(HttpStatus.BAD_REQUEST, "Extensão de arquivo não suportada.");
    }


}
