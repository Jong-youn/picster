package jake.pin.scheduler;

import jakarta.annotation.PreDestroy;
import jake.pin.controller.model.response.ImageRes;
import jake.pin.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageScheduler {

    private final CacheManager cacheManager;
    private final ImageRepository repository;

    @Scheduled(fixedRate = 60000)
    @PreDestroy
    public void updateViewCountInDatabase() {
        log.info("[조회수 업데이트 스케줄러 시작]");
        Cache cache = cacheManager.getCache("getImage");
        if (cache != null) {
            ConcurrentHashMap<Long, ImageRes> nativeCache = (ConcurrentHashMap) cache.getNativeCache();
            for (Long key : nativeCache.keySet()) {
                ImageRes cachedImage = nativeCache.get(key);
                if (cachedImage != null) {
                    repository.updateViewCount(key, cachedImage.getViewCount());
                }
            }
        }
        log.info("[조회수 업데이트 스케줄러 종료]");
    }

}
