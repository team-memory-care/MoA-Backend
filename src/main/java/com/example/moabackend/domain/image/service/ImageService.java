package com.example.moabackend.domain.image.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<String> upload (List<MultipartFile> files);
}
