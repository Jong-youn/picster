package jake.pin.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageCreateDto {

    private long userId;
    private String imageURL;
    private String title;
    private String description;
}
