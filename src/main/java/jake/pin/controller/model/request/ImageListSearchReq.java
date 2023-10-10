package jake.pin.controller.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageListSearchReq {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private int limit = DEFAULT_LIMIT;
    private int pageNo = 0;

    public void valid() {
        if (this.limit < 1 || this.limit > MAX_LIMIT) {
            this.limit = DEFAULT_LIMIT;
        }
        if (this.pageNo < 0) {
            this.pageNo = 0;
        }
    }
}
