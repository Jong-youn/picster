package jake.pin.service;

import jake.pin.controller.model.request.ImageListSearchReq;
import jake.pin.controller.model.response.ImageRes;
import jake.pin.repository.ImageRepository;
import jake.pin.repository.entity.Image;
import jake.pin.repository.entity.ImageListSearch;
import jake.pin.service.dto.ImageCreateDto;
import jake.pin.service.dto.ImageUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    public void remove(long imageId, long userId) {
        // 유저가 포스팅한 이미지인지 확인
        Image image = repository.getImageByIdAndUserId(imageId, userId);
        if (image == null) {
            log.warn("부적절한 이미지 삭제시도가 의심됩니다.");
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다.");
        }

        // 데이터베이스에서 소프트 딜리트
        Image deletedImage = Image.builder()
                .id(imageId)
                .deletedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .userId(userId)
                .build();
        int removeResult = repository.remove(deletedImage);
        if (removeResult < 1) {
            throw new RuntimeException("이미지 삭제에 실패했습니다. 다시 시도해주세요.");
        }

        // 스토리지에서 데이터 삭제
        boolean result = removeInStorage(image.getImageUrl());
        if (!result) {
            log.warn("이미지를 스토리지에서 삭제하는데 실패했습니다. imageId: " + imageId);
        }
    }

    public Page<ImageRes> getImages(ImageListSearchReq request) {
        ImageListSearch search = new ImageListSearch();
        search.setLimit(request.getLimit());
        search.setOffset(request.getPageNo() * request.getLimit());
        search.setPage(request.getPageNo());

        Page<Image> entities = repository.getImages(search);

        List<ImageRes> responses = convertToRes(entities.getContent());
        return new PageImpl<>(responses, entities.getPageable(), entities.getTotalElements());
    }

    private boolean removeInStorage(String imageUrl) {
        try {
            File fileToDelete = new File(imageUrl);
            if (fileToDelete.exists() && fileToDelete.isFile()) {
                return fileToDelete.delete();
            } else {
                log.info("[ImageService:removeInStorage] 파일을 찾을 수 없거나 디렉토리입니다 imageUrl: " + imageUrl);
                return false;
            }
        } catch (Exception e) {
            log.info("[ImageService:removeInStorage] msg: " + e.getMessage(), e);
            return false;
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

    private List<ImageRes> convertToRes(List<Image> images) {
        List<ImageRes> list = new ArrayList<>();
        for (Image image : images) {
            list.add(convertToRes(image));
        }

        return list;
    }

    private ImageRes convertToRes(Image image) {
        if (image == null) return null;

        return ImageRes.builder()
                .id(image.getId())
                .thumbnailURL(image.getImageUrl())
                .title(image.getTitle())
                .viewCount(image.getViewCount())
                .build();
    }
}
