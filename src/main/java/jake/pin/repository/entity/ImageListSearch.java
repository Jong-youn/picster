package jake.pin.repository.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageListSearch {

    private int limit;
    private int offset;
    private int page;
}
