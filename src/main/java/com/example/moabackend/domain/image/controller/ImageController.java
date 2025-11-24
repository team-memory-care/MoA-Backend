package com.example.moabackend.domain.image.controller;

import com.example.moabackend.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> s3Upload(@RequestPart(value = "image") List<MultipartFile> multipartFile) {
        List<String> upload = imageService.upload(multipartFile);
        return ResponseEntity.ok(upload);
    }
}
