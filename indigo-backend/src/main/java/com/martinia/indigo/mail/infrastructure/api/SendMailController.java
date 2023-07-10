package com.martinia.indigo.mail.infrastructure.api;

import com.martinia.indigo.mail.domain.ports.usecases.SendMailUseCase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/mail")
public class SendMailController {

	@Resource
	private SendMailUseCase useCase;

	@GetMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> send(@RequestParam final String path, @RequestParam final String address) throws Exception {

		final String error = useCase.mail(path, address);

		if (StringUtils.isEmpty(error)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		else {
			throw new Exception(error);
		}
	}

}
