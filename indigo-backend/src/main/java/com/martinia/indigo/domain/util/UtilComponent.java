package com.martinia.indigo.domain.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Slf4j
@Component
public class UtilComponent {

	@Value("${book.library.path}")
	private String libraryPath;

	public String getBase64Cover(String path) {
		String image = null;

		if (!libraryPath.endsWith(File.separator))
			libraryPath += File.separator;

		path = libraryPath + path;

		if ((new File(path)).exists()) {
			try {
				String coverPath = path + "/cover.jpg";
				String thumbPath = path + "/thumbails.jpg";

				File thumbFile = new File(thumbPath);

				if (thumbFile.exists()) {
					thumbFile.delete();
				}

				File coverFile = new File(coverPath);
				BufferedImage originalImage = ImageIO.read(coverFile);

				int h = originalImage.getHeight();
				int w = originalImage.getWidth();

				if (h > w) {
					h = 250;
					w = (w * 250) / h;
				} else {
					w = 250;
					h = (h * 250) / w;
				}

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Thumbnails.of(originalImage)
						.size(w, h)
						.outputFormat("jpg")
						.toOutputStream(outputStream);

				image = Base64.getEncoder()
						.encodeToString(outputStream.toByteArray());
			} catch (Exception e) {
				log.debug(e.getMessage());
			}
		}

		return image;
	}

	public String getBase64Url(String image) {

		String ret = null;
		if (StringUtils.isNoneEmpty(image)) {
			if (!image.equals(
					"https://s.gr-assets.com/assets/nophoto/user/u_200x266-e183445fd1a1b5cc7075bb1cf7043306.png")) {

				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(image).openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0");

					BufferedImage originalImage = ImageIO.read(connection.getInputStream());

					int h = originalImage.getHeight();
					int w = originalImage.getWidth();

					if (h > w) {
						h = 250;
						w = (w * 250) / h;
					} else {
						w = 250;
						h = (h * 250) / w;
					}

					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					Thumbnails.of(originalImage)
							.size(w, h)
							.outputFormat("jpg")
							.toOutputStream(outputStream);

					ret = Base64.getEncoder()
							.encodeToString(outputStream.toByteArray());

				} catch (Exception e) {
					log.error(image + " --> " + e.getMessage());
				}
			}
		}

		return ret;

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
