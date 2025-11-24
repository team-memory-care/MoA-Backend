package com.example.moabackend.domain.image.service;

import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // [public 메서드] 외부에서 사용, S3에 저장된 이미지 객체의 public url을 반환
    public List<String> upload(List<MultipartFile> files) {
        return files.stream()
                .map(this::uploadImage)
                .toList();
    }

    //validateFile메서드를 호출하여 유효성 검증 후 uploadImageToS3메서드에 데이터를 반환하여 S3에 파일 업로드,
    // public url을 받아 서비스 로직에 반환
    private String uploadImage(MultipartFile file) {
        validateFile(file.getOriginalFilename());
        return uploadImageToS3(file);
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
        List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png", "gif");
        if (extension == null || !allowedExtentionList.contains(extension)) {
            throw new CustomException(GlobalErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    // 직접 S3에 업로드
    private String uploadImageToS3(MultipartFile file) {
        // 원본 파일 명
        String originalFileName = file.getOriginalFilename();
        // 변경된 파일
        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + "_" + originalFileName;

        // 이미지 파일 -> InputStream 변환
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3FileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()
            ));
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new CustomException(GlobalErrorCode.IO_EXCEPTION_UPLOAD_FILE);
        }
        return s3FileName;
    }
}
