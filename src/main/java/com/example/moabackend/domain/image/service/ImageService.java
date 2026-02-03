package com.example.moabackend.domain.image.service;

import com.example.moabackend.domain.image.entity.MoaImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    // 저장 후 ID 목록을 반환하도록 변경
    List<Long> upload(List<MultipartFile> files);

    // 사진 꺼내는 코드
    MoaImage findImage(Long id);
}
