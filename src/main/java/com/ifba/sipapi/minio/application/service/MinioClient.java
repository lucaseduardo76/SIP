package com.ifba.sipapi.minio.application.service;

import com.ifba.sipapi.user.domain.User;
import org.springframework.web.multipart.MultipartFile;

public interface MinioClient {
    String uploadUserProfileImage(MultipartFile profileImage, User user);
}
