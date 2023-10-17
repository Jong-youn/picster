package jake.pin.service;

import jake.pin.repository.ImageRepository;
import jake.pin.repository.entity.Image;
import jake.pin.repository.entity.User;
import jake.pin.service.dto.ImageCreateDto;
import jake.pin.service.dto.ImageUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService service;
    @Mock
    private ImageRepository repository;

    @Nested
    @DisplayName("이미지 저장시")
    class saveTest {
        @Test
        @DisplayName("정상 작동")
        void saveImageSuccess() {
            // given
            User user = getUser();
            ImageCreateDto dto = getImageCreateDto(user.getId());

            // stub
            when(repository.save(any(Image.class))).thenReturn(1L);

            // when
            CompletableFuture<Long> result = service.create(dto);
            Long imageId = result.join();

            // then
            assertEquals(1L, imageId);
        }

        @Test
        @DisplayName("이미지 주소를 입력하지 않으면 예외 발생")
        void saveImageWithoutUrl() {
            // given
            ImageCreateDto dto = getImageCreateDtoWithoutImageUrl();

            // when, then
            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("이미지 정보를 디비에 저장하는 것에 실패하면 예외 발생하고 파일 삭제")
        void failToSaveOnDB() {
            // given
            User user = getUser();
            ImageCreateDto dto = getImageCreateDto(user.getId());

            // stub, when
            given(repository.save(any(Image.class))).willReturn(0l);

            // then
            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("이미지 정보 변경시")
    class modifyTest {
        @Test
        @DisplayName("정상 작동")
        void modify() {
            // given
            ImageUpdateDto dto = getImageUpdateDto();
            Image image = getImage(dto.getUserId());

            // stub, when
            given(repository.getImageByIdAndUserId(dto.getId(), dto.getUserId())).willReturn(image);
            given(repository.modify(any(Image.class))).willReturn(1);

            // then
            service.modify(dto);
        }

        @Test
        @DisplayName("다른 유저가 스크랩한 이미지를 변경시도하면 예외가 발생한다")
        void modifyIllegalImage() {
            // given
            ImageUpdateDto dto = getImageUpdateDto();

            // stub
            given(repository.getImageByIdAndUserId(dto.getId(), dto.getUserId())).willReturn(null);

            // then
            assertThatThrownBy(() -> service.modify(dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("디비 업데이트에 실패하면 예외가 발생한다")
        void NoModifyIfDBUpdateFail() {
            // given
            ImageUpdateDto dto = getImageUpdateDto();
            Image image = getImage(dto.getUserId());

            // stub
            given(repository.getImageByIdAndUserId(dto.getId(), dto.getUserId())).willReturn(image);
            given(repository.modify(any(Image.class))).willReturn(0);

            // then
            assertThatThrownBy(() -> service.modify(dto))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("이미지 삭제시")
    class removeTest {
        @Test
        @DisplayName("다른 유저가 스크랩한 이미지를 삭제시도하면 예외가 발생한다")
        void removeIllegalImage() {
            // given
            User user = getUser();
            Image image = getImage(user.getId());

            // stub
            given(repository.getImageByIdAndUserId(image.getId(), user.getId())).willReturn(null);

            // then
            assertThatThrownBy(() -> service.remove(image.getId(), user.getId()))
                    .isInstanceOf(IllegalArgumentException.class);

        }

        @Test
        @DisplayName("이미지 삭제에 실패하면 예외가 발생한다")
        void NoModifyIfDBUpdateFail() {
            // given
            User user = getUser();
            Image image = getImage(user.getId());

            // stub
            given(repository.getImageByIdAndUserId(image.getId(), user.getId())).willReturn(image);
            given(repository.remove(image)).willReturn(0);

            // then
            assertThatThrownBy(() -> service.remove(image.getId(), user.getId()))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    private ImageCreateDto getImageCreateDto(long userId) {
        String url = "https://www.adorama.com/alc/wp-content/uploads/2017/11/shutterstock_114802408-825x465.jpg";
        return ImageCreateDto.builder()
                .userId(userId)
                .imageURL(url)
                .build();
    }

    private ImageCreateDto getImageCreateDtoWithoutImageUrl() {
        return ImageCreateDto.builder()
                .userId(1L)
                .title("title")
                .description("description")
                .build();
    }

    private ImageUpdateDto getImageUpdateDto() {
        return ImageUpdateDto.builder()
                .id(1L)
                .userId(1L)
                .build();
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setName("user1");
        return user;
    }

    private Image getImage(long userId) {
        return Image.builder()
                .id(1L)
                .userId(userId)
                .build();
    }
}