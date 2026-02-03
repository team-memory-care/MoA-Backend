package com.example.moabackend.domain.image.service;

import com.example.moabackend.domain.image.entity.MoaImage;
import com.example.moabackend.domain.image.repository.ImageRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    //  이미지를 DB에 저장하고, 저장된 ID 리스트를 반환
    @Override
    @Transactional
    public List<Long> upload(List<MultipartFile> files) {
        return files.stream()
                .map(this::uploadImageToDb)
                .toList();
    }

    // 내부 로직: 검증 -> 변환 -> DB 저장 -> ID 반환
    private Long uploadImageToDb(MultipartFile file) {
        // 1. 유효성 검증 유지
        validateFile(file.getOriginalFilename());

        // 2. DB 저장 수행
        try {
            MoaImage moaImage = MoaImage.builder()
                    .originalFileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .imageData(file.getBytes())
                    .build();

            MoaImage savedImage = imageRepository.save(moaImage);
            return savedImage.getId();
        } catch (IOException e) {
            throw new CustomException(GlobalErrorCode.IO_EXCEPTION_UPLOAD_FILE);
        }
    }

    // 파일 유효성 검증
    private void validateFile(String fileName) {
        // 파일 존재 유무 검증
        if (fileName == null || fileName.isEmpty()) {
            throw new CustomException(GlobalErrorCode.NOT_EXIST_FILE);
        }

        // 확장자 존재 유무 검증
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new CustomException(GlobalErrorCode.NOT_EXIST_FILE_EXTENSION);
        }

        // 허용되지 않는 확장자 검증
        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();

        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(GlobalErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    @Override
    public MoaImage findImage(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_EXIST_FILE));
    }
}
