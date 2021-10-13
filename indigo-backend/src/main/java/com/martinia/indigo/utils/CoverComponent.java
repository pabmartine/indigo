package com.martinia.indigo.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.martinia.indigo.services.calibre.SerieService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CoverComponent {

	@Autowired
	private SerieService serieService;

	@Value("${book.library.path}")
	private String libraryPath;

	public String getCover(int id) throws IOException {

		if (!libraryPath.endsWith(File.separator))
			libraryPath += File.separator;

		String path = serieService.getBookPathSerieById(id, PageRequest.of(0, 1, Sort.by("seriesIndex")))
				.get(0);

		String basePath = libraryPath + path + "/cover.jpg";

		File file = new File(basePath);

		String image = Base64.getEncoder()
				.encodeToString(Files.readAllBytes(file.toPath()));

		return image;

	}

	public String getCover(String path, boolean force) throws IOException {
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

		String image = Base64.getEncoder()
				.encodeToString(Files.readAllBytes(thumbFile.toPath()));

		return image;
	}

}
