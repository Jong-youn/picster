package jake.pin.controller;

import jake.pin.controller.model.request.ImageCreateReq;
import jake.pin.controller.model.request.ImageListSearchReq;
import jake.pin.controller.model.request.ImageUpdateReq;
import jake.pin.controller.model.response.CommonListRes;
import jake.pin.controller.model.response.ImageCreateRes;
import jake.pin.controller.model.response.ImageRes;
import jake.pin.service.ImageService;
import jake.pin.service.UserService;
import jake.pin.service.dto.ImageCreateDto;
import jake.pin.service.dto.ImageUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService service;
    private final UserService userService;

    @PostMapping
    public CompletableFuture<ImageCreateRes> create(@RequestHeader("Authorization") Long userId,
                                                    @RequestBody ImageCreateReq request) {
        validateUserId(userId);
        ImageCreateDto dto = ImageCreateDto.builder()
                .userId(userId)
                .imageURL(request.getImageURL())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return service.create(dto).thenApply(ImageCreateRes::new);
    }

    @PatchMapping("/{imageId}")
    public void modify(@RequestHeader("Authorization") Long userId,
                       @PathVariable Long imageId,
                       @RequestBody ImageUpdateReq request) {
        validateUserId(userId);
        validateImageId(imageId);
        ImageUpdateDto dto = ImageUpdateDto.builder()
                .userId(userId)
                .id(imageId)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        service.modify(dto);
    }

    @DeleteMapping("/{imageId}")
    public void remove(@RequestHeader("Authorization") Long userId,
                       @PathVariable Long imageId) {
        validateUserId(userId);
        validateImageId(imageId);

        service.remove(imageId, userId);
    }

    @GetMapping
    public CommonListRes<ImageRes> getImages(ImageListSearchReq request) {
        request.valid();
        Page<ImageRes> images =  service.getImages(request);

        CommonListRes<ImageRes> response = new CommonListRes<>(images.getContent());
        response.setCurrentCount(images.getContent().size());
        response.setCurrentPage(images.getPageable().getPageNumber());
        response.setTotalCount(images.getTotalElements());
        response.setTotalPages(images.getTotalPages());
        return response;
    }

    private void validateUserId(Long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("로그인이 필요한 기능입니다.");
        }

        if (userService.getUserById(userId) == null) {
            throw new IllegalArgumentException("회원가입 후 이용해주시기 바랍니다.");
        }
    }

    private void validateImageId(long imageId) {
        if (imageId <= 0) {
            throw new IllegalArgumentException("잘못된 접근입니다. 다시 시도해주세요.");
        }
    }
}
