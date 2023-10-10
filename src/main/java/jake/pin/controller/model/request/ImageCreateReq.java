package jake.pin.controller.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageCreateReq {

    private String imageURL;
    private String title;
    private String description;
}
