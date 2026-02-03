package com.example.moabackend.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "MOA_IMAGES")
public class MoaImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;

    private String contentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Builder
    public MoaImage(String originalFileName, String contentType, byte[] imageData) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.imageData = imageData;
    }
}
