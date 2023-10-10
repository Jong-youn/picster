package jake.pin.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUpdateDto {

    private long id;
    private long userId;
    private String title;
    private String description;
}
