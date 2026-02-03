package com.example.moabackend.domain.image.repository;

import com.example.moabackend.domain.image.entity.MoaImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<MoaImage, Long> {
}