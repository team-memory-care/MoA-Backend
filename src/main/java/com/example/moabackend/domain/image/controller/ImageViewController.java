package com.example.moabackend.domain.image.controller;

import com.example.moabackend.domain.image.entity.MoaImage;
import com.example.moabackend.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageViewController {

    private final ImageService imageService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> viewImage(@PathVariable Long id) {
        MoaImage image = imageService.findImage(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getImageData());
    }
}
