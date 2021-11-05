package com.martinia.indigo.domain.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UtilComponent {

	@Value("${book.library.path}")
	private String libraryPath;

	public String getCover(String path, boolean force) {
		String image = null;

		if (!libraryPath.endsWith(File.separator))
			libraryPath += File.separator;

		path = libraryPath + path;

		if ((new File(path)).exists()) {
			try {
				String coverPath = path + "/cover.jpg";
				String thumbPath = path + "/thumbails.jpg";

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
				log.debug(e.getMessage());
			}
		}

		return image;
	}

	public Resource getEpub(String path) {

		Resource epub = null;

		try {
			if (!libraryPath.endsWith(File.separator))
				libraryPath += File.separator;

			String basePath = libraryPath + path;

			File file = new File(basePath);
			if (file.exists()) {
				File[] files = file.listFiles();
				for (File f : files) {
					if (f.getName()
							.endsWith(".epub")) {
						epub = new UrlResource(f.toPath()
								.toUri());
						break;
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return epub;
	}

}
