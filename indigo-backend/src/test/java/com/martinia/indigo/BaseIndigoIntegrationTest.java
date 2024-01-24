package com.martinia.indigo;

import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

public class BaseIndigoIntegrationTest extends BaseIndigoTest {

	@Resource
	protected MockMvc mockMvc;
}
