package kr.spot.exception;

import io.sentry.Sentry;
import kr.spot.ApiResponse;
import kr.spot.code.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(GeneralException.class)
    public ApiResponse<ErrorStatus> baseExceptionHandle(GeneralException exception) {
        log.warn("BaseException. error message: {}", exception.getMessage());
        return new ApiResponse<>(exception.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<ErrorStatus> exceptionHandle(Exception exception) {
        log.error("Exception has occurred:  {}", exception);
        Sentry.captureException(exception);
        return new ApiResponse<>(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
}