package com.martinia.indigo.common.error.infrastructure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ErrorRedirectControllerTest {

	@Test
	public void testError_shouldReturnForwardToRoot() {
		// Arrange
		ErrorRedirectController errorRedirectController = new ErrorRedirectController();

		// Act
		String result = errorRedirectController.error();

		// Assert
		Assertions.assertNotNull(result);
		Assertions.assertEquals("forward:/", result);
	}
}