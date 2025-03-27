package tech.kp45.bids.bridge.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicException;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Slf4j
@ControllerAdvice
public class GlobalApiExcetionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File upload error.");
    }

    @ExceptionHandler(BasicRuntimeException.class)
    public ResponseEntity<String> handleBusinessRuntimeError(BasicRuntimeException e) {
        log.error("Server Internal Runtime Error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(BasicException.class)
    public ResponseEntity<String> handleBusinessServerError(BasicException e) {
        log.error("Server Internal Error", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("System Internal error.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleServerError(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value()).body("System Internal error.");
    }
}
