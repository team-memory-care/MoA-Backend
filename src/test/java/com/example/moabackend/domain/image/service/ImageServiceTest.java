package com.example.moabackend.domain.image.service;

import com.example.moabackend.domain.image.entity.MoaImage;
import com.example.moabackend.domain.image.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("로컬의 이미지를 읽어 오라클 DB에 업로드한다")
//    @Transactional
    void localImageUploadTest() throws Exception {

        // 1. 파일 경로 설정 (사용자명 gimdonghyeon 에 맞춰 수정함)
        String filePath = "/Users/gimdonghyeon/Desktop/testImage.jpeg";
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다. 경로를 확인해주세요");
        }

        // 2. 실제 파일을 읽어서 MockMultipartFile로 변환
        FileInputStream inputStream = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                file.getName(),
                "image/jpeg",
                inputStream
        );

        System.out.println("======== 업로드 시작 ========");

        // 3. 서비스 호출 (DB 저장)
        List<Long> savedIds = imageService.upload(List.of(multipartFile));

        System.out.println("======== 업로드 종료 =========");
        System.out.println("저장된 이미지 ID: " + savedIds.get(0));

        // 4. 검증 (DB에서 다시 조회해서 잘 들어갔는지 확인)
        MoaImage savedImage = imageRepository.findById(savedIds.get(0)).orElseThrow();

        assertThat(savedImage.getOriginalFileName()).isEqualTo("testImage.jpeg");

        System.out.println("검증 완료: 데이터가 정상적으로 BLOB 컬럼에 저장되었습니다.");
    }
}
