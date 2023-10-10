package jake.pin.controller;

import jake.pin.controller.model.request.ImageCreateReq;
import jake.pin.controller.model.response.ImageCreateRes;
import jake.pin.service.ImageService;
import jake.pin.service.UserService;
import jake.pin.service.dto.ImageCreateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private void validateUserId(Long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("로그인이 필요한 기능입니다.");
        }

        if (userService.getUserById(userId) == null) {
            throw new IllegalArgumentException("회원가입 후 이용해주시기 바랍니다.");
        }
    }
}
