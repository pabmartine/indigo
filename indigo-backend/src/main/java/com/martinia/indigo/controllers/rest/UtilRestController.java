package com.martinia.indigo.controllers.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.services.MailService;
import com.martinia.indigo.services.indigo.ConfigurationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/util")
public class UtilRestController {

	@Autowired
	MailService mailService;

	@Autowired
	private ConfigurationService configurationService;

	@Value("${book.library.path}")
	private String libraryPath;

	@GetMapping(value = "/testmail", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> testmail(@RequestParam int user) {
		mailService.testEmail(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	@GetMapping(value = "/mail", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> mail(@RequestParam String path, @RequestParam int user) throws Exception {

		String error = null;

		String kindlegenPath = configurationService.findById("kindlegen.path")
				.get()
				.getValue();

		if (!libraryPath.endsWith(File.separator))
			libraryPath += File.separator;

		String basePath = libraryPath + path;

		File file = new File(basePath);
		if (file.exists()) {
			File[] files = file.listFiles();
			File epub = null;
			File mobi = null;
			for (File f : files) {
				if (f.getName()
						.endsWith(".epub"))
					epub = f;
				else if (f.getName()
						.endsWith(".mobi"))
					mobi = f;
			}

			if (mobi == null) {
				try {

					String name = epub.getName()
							.substring(0, epub.getName()
									.indexOf("."));
					String newName = Base64.getEncoder()
							.encodeToString(name.getBytes()) + ".epub";

					File destination = new File(libraryPath + newName);

					Files.copy(epub.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

					String cmd = kindlegenPath + " " + destination.getPath();
					log.info(cmd);
					Process process = Runtime.getRuntime()
							.exec(cmd);
					InputStream is = null;
					BufferedReader br = null;
					InputStreamReader isr = null;
					String textLine = "";
					is = process.getInputStream();
					isr = new InputStreamReader(is);
					br = new BufferedReader(isr);
					while ((textLine = br.readLine()) != null) {
						if (0 != textLine.length()) {
							log.info(textLine);
						}
					}

					mobi = new File(destination.getPath()
							.replace(".epub", ".mobi"));

					destination.delete();

				} catch (IOException e) {
					error = e.getMessage();
					log.error(error);
				}
			}

			if (mobi != null) {
				try {
					mailService.sendEmail(mobi.getName(), mobi, user);
				} catch (Exception e) {
					error = e.getMessage();
					log.error(error);
				}
			}

		}

		if (StringUtils.isEmpty(error)) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			throw new Exception(error);
		}
	}

}
