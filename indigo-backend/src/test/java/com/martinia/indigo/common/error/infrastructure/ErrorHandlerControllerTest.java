package com.martinia.indigo.common.error.infrastructure;

import com.martinia.indigo.common.error.infrastructure.model.ErrorMessageDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class ErrorHandlerControllerTest {

	@Test
	public void testGlobalExceptionHandler_withIOException_shouldReturnNotFoundResponse() {
		// Arrange
		IOException exception = new IOException("File not found");
		WebRequest webRequest = mock(WebRequest.class);

		ErrorHandlerController errorHandlerController = new ErrorHandlerController();

		// Act
		ResponseEntity<ErrorMessageDto> response = errorHandlerController.globalExceptionHandler(exception, webRequest);

		// Assert
		Assertions.assertNotNull(response);
		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		ErrorMessageDto message = response.getBody();
		Assertions.assertNotNull(message);
		Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), message.getStatusCode());
		Assertions.assertEquals(exception.getMessage(), message.getMessage());
		Assertions.assertEquals(webRequest.getDescription(false), message.getDescription());
	}

	@Test
	public void testGlobalExceptionHandler_withException_shouldReturnInternalServerErrorResponse() {
		// Arrange
		Exception exception = new Exception("Internal server error");
		WebRequest webRequest = mock(WebRequest.class);

		ErrorHandlerController errorHandlerController = new ErrorHandlerController();

		// Act
		ResponseEntity<ErrorMessageDto> response = errorHandlerController.globalExceptionHandler(exception, webRequest);

		// Assert
		Assertions.assertNotNull(response);
		Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

		ErrorMessageDto message = response.getBody();
		Assertions.assertNotNull(message);
		Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), message.getStatusCode());
		Assertions.assertEquals(exception.getMessage(), message.getMessage());
		Assertions.assertEquals(webRequest.getDescription(false), message.getDescription());
	}
}
