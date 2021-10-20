package com.martinia.indigo.domain.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CoverComponent {

	@Value("${book.library.path}")
	private String libraryPath;

	public String getCover(String path, boolean force) {
		String image = null;
		try {
			if (!libraryPath.endsWith(File.separator))
				libraryPath += File.separator;

			String coverPath = libraryPath + path + "/cover.jpg";
			String thumbPath = libraryPath + path + "/thumbails.jpg";

			File thumbFile = new File(thumbPath);

			if (!thumbFile.exists() || force) {

				File coverFile = new File(coverPath);

				BufferedImage i = ImageIO.read(coverFile);
				BufferedImage scaledImg = Scalr.resize(i, 250);

				ImageIO.write(scaledImg, "JPG", thumbFile);
			}

			image = Base64.getEncoder()
					.encodeToString(Files.readAllBytes(thumbFile.toPath()));
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return image;
	}

}
