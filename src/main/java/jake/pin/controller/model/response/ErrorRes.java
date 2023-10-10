package jake.pin.controller.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorRes {

    private String errorMsg;

    public ErrorRes(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
