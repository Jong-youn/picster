package jake.pin.service;

import jake.pin.repository.ImageRepository;
import jake.pin.repository.entity.Image;
import jake.pin.service.dto.ImageCreateDto;
import jake.pin.service.dto.ImageUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository repository;
//    private final CacheManager cacheManager;
    @Value("${storage.path}")
    private String STORAGE_PATH;

    @Async
    public CompletableFuture<Long> create(ImageCreateDto dto) {
        // 이미지 다운로드
        String fileUrl = downloadImage(dto.getImageURL());

        // 이미지 데이터 저장
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Image image = Image.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(fileUrl)
                .createdAt(now)
                .build();

        long imageId = repository.save(image);
        if (imageId < 1) {
            log.info("이미지 저장에 실패했습니다.");
            throw new RuntimeException("이미지 저장에 실패했습니다. 다시 시도해주세요.");
        }

        return CompletableFuture.completedFuture(imageId);
    }

    @Transactional
    public void modify(ImageUpdateDto dto) {
        // 유저가 포스팅한 이미지인지 확인
        Image findImage = repository.getImageByIdAndUserId(dto.getId(), dto.getUserId());
        if (findImage == null) {
            log.warn("부적절한 이미지 변경시도가 의심됩니다.");
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다.");
        }

        // 데이터 변경
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Image image = Image.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .updatedAt(now)
                .build();
        int result = repository.modify(image);
        if (result < 1) {
            throw new RuntimeException("수정에 실패했습니다. 다시 시도해주세요.");
        }
    }

    private String downloadImage(String url) {
        try {
            InputStream inputStream = new URL(url).openStream();
            String fileName = generateFileName();

            File directory = new File(STORAGE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String extension = getExtension(url);
            String savedFilePath = STORAGE_PATH + File.separator + fileName + extension;
            Files.copy(inputStream, Path.of(savedFilePath));
            // inputStream.close(); 사용이 끝나면 인풋 스트림 자원을 해제해야 함.
            return new File(savedFilePath).getAbsolutePath();
        } catch (IOException e) {
            log.warn("다운로드 받는 과정중에 에러가 발생했습니다." + e.getMessage());
            throw new RuntimeException("이미지 저장에 실패했습니다. 다시 시도해주세요.", e);
        }
    }

    private String generateFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timeStamp = dateFormat.format(new Date());

        String randomString = UUID.randomUUID().toString().replace("-", "");

        return timeStamp + randomString;
    }

    private String getExtension(String imageUrl) {
        String[] parts = imageUrl.split("\\.");
        if (parts.length > 1) {
            return "." + parts[parts.length - 1];
        }

        // 확장자 기본값: jpg
        return ".jpg";
    }
}
