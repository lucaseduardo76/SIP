package com.ifba.sipapi.minio.api.service;

import com.ifba.sipapi.item.domain.item.Item;
import com.ifba.sipapi.user.domain.User;
import org.springframework.web.multipart.MultipartFile;

public interface MinioClient {
    String uploadUserProfileImage(MultipartFile profileImage, User user);
    String uploadItemsImage(MultipartFile profileImage, Item item);
    void deleteItemImage(String imageUrl);
}
