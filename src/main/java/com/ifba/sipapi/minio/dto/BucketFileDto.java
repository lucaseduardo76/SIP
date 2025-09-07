package com.ifba.sipapi.minio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BucketFileDto {
    private final String bucket;
    private final String filename;
}
