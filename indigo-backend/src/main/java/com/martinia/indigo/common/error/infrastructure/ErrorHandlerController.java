package com.martinia.indigo.common.error.infrastructure;

import com.martinia.indigo.common.error.infrastructure.model.ErrorMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.Date;


@Slf4j
@ControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorMessageDto> globalExceptionHandler(IOException ex, WebRequest request) {
        ErrorMessageDto message = new ErrorMessageDto(HttpStatus.NOT_FOUND.value(), new Date(), ex.getMessage(),
                request.getDescription(false));
        log.error(ex.getMessage());
        return new ResponseEntity<ErrorMessageDto>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageDto> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorMessageDto message = new ErrorMessageDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), ex.getMessage(),
                request.getDescription(false));
        log.error(ex.getMessage());
        return new ResponseEntity<ErrorMessageDto>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}