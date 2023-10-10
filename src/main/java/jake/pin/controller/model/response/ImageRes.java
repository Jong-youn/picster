package jake.pin.controller.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImageRes {

    private long id;
    private String thumbnailURL;
    private String title;
    private int viewCount;
}
