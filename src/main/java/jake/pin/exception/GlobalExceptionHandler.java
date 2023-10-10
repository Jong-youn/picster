package jake.pin.exception;

import jake.pin.controller.model.response.ErrorRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({RuntimeException.class, Exception.class})
    protected ResponseEntity<ErrorRes> handleRuntimeException(Exception e) {
        log.info(e.getMessage(), e);
        return new ResponseEntity<>(new ErrorRes(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<ErrorRes> handleMissingRequestHeaderException(Exception e) {
        log.error("Header에 {}이 입력되지 않았습니다.", ((MissingRequestHeaderException) e).getHeaderName(), e);
        return new ResponseEntity<>(new ErrorRes("관리자에게 문의 바랍니다."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorRes> handleIllegalArgumentException(Exception e) {
        log.info(e.getMessage(), e);
        return new ResponseEntity<>(new ErrorRes(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
