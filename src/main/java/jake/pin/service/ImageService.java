package jake.pin.service;

import jake.pin.controller.model.request.ImageListSearchReq;
import jake.pin.controller.model.response.ImageRes;
import jake.pin.repository.ImageRepository;
import jake.pin.repository.entity.Image;
import jake.pin.repository.entity.ImageListSearch;
import jake.pin.service.dto.ImageCreateDto;
import jake.pin.service.dto.ImageUpdateDto;
import jake.pin.utils.FileCreator;
import jake.pin.utils.StorageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository repository;
    private final CacheManager cacheManager;

    @Async
    public CompletableFuture<Long> create(ImageCreateDto dto) {
        // 이미지 다운로드 및 스토리지에 저장
        byte[] fileUrl = FileCreator.download(dto.getImageURL());
        String fileName = FileCreator.generateFileName(dto.getImageURL());
        String path = StorageHelper.save(fileUrl, fileName);

        // 이미지 데이터 저장
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Image image = Image.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(path)
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
        boolean result = StorageHelper.remove(image.getImageUrl());
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

//    @Cacheable(value = "getImage", key = "#imageId")
    public ImageRes getCachedImage(long imageId) {
        Image image = repository.getImageById(imageId);
        if (image == null) {
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다.");
        }

        return convertToRes(image);
    }

    public void updateViewCountInCache(long imageId) {
        Cache cache = cacheManager.getCache("getImage");
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(imageId);
            if (valueWrapper != null) {
                ImageRes image = (ImageRes) valueWrapper.get();
                if (image != null) {
                    // 이미지 객체의 조회수만 업데이트
                    image.setViewCount(image.getViewCount() + 1);
                }
            }
        }
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
