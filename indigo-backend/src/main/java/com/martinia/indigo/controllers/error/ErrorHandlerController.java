package com.martinia.indigo.controllers.error;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.martinia.indigo.dto.ErrorMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller Error Handler
 *
 */
@Slf4j
@ControllerAdvice
public class ErrorHandlerController {
	
	/**
	 * Manage IO errors
	 * 
	 * @param ex      the exception
	 * @param request the request
	 * @return
	 */
	@ExceptionHandler(IOException.class)
	public ResponseEntity<ErrorMessage> globalExceptionHandler(IOException ex, WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND.value(), new Date(), ex.getMessage(),
				request.getDescription(false));
		log.error(ex.getMessage());
		return new ResponseEntity<ErrorMessage>(message, HttpStatus.NOT_FOUND);
	}

	/**
	 * Manage global errors
	 * 
	 * @param ex      the exception
	 * @param request the request
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), ex.getMessage(),
				request.getDescription(false));
		log.error(ex.getMessage());
		return new ResponseEntity<ErrorMessage>(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}