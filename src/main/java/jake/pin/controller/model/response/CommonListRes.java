package jake.pin.controller.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommonListRes<T> {

    private List<T> data;
    private long currentCount;
    private int currentPage;
    private long totalCount;
    private int totalPages;

    public CommonListRes(List<T> data) {
        this.data = data;
    }
}
