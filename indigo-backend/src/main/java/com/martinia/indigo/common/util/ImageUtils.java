package com.martinia.indigo.common.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Component
public class ImageUtils {

	@Value("${book.library.path}")
	private String libraryPath;

	public String getImageFromEpub(String path, String... types) {

		log.debug("Getting base64 image cover in {}", path);

		String image = null;
		try {
			if (!libraryPath.endsWith(File.separator)) {
				libraryPath += File.separator;
			}

			String basePath = libraryPath + path;

			File file = new File(basePath);
			if (file.exists()) {
				File[] files = file.listFiles();
				for (File f : files) {
					if (f.getName().endsWith(".epub")) {

						try (ZipFile zipFile = new ZipFile(f)) {
							Enumeration zipFiles = zipFile.entries();

							while (zipFiles.hasMoreElements()) {
								ZipEntry entry = (ZipEntry) zipFiles.nextElement();
								if (!entry.isDirectory()) {

									String fileName = entry.getName().toLowerCase();
									String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

									for (String type : types) {
										if (fileName.contains(type) && Arrays.asList("jpg", "jpeg", "png").contains(extension)) {
											System.out.println("File " + entry.getName());
											try (InputStream is = zipFile.getInputStream(entry)) {
												BufferedImage originalImage = ImageIO.read(is);
												image = getScaledImage(originalImage, 0);
											}
										}
									}

								}

							}
						}

					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	public String getBase64Cover(String path, boolean scale) {
		String image = null;

		if (!libraryPath.endsWith(File.separator)) {
			libraryPath += File.separator;
		}

		path = libraryPath + path;

		if ((new File(path)).exists()) {
			try {
				String coverPath = path + "/cover.jpg";
				String thumbPath = path + "/thumbnail.jpg";

				File thumbFile = new File(thumbPath);

				if (thumbFile.exists()) {
					thumbFile.delete();
				}

				File coverFile = new File(coverPath);
				BufferedImage originalImage = ImageIO.read(coverFile);
				if (scale) {
					image = getScaledImage(originalImage, 0);
				}
				else {
					image = getOriginalImage(originalImage);
				}
			}
			catch (Exception e) {
				log.debug(e.getMessage());
			}
		}

		if (image == null) {
			image = getImageFromEpub(path, "cover", "thumbnail");
		}

		return image;
	}

	public String getBase64Cover(InputStream inputStream, boolean scale) {
		String image = null;

		try {
			BufferedImage originalImage = ImageIO.read(inputStream);

			if (scale) {
				image = getScaledImage(originalImage, 0);
			}
			else {
				image = getOriginalImage(originalImage);
			}
		}
		catch (IOException e) {
			log.debug(e.getMessage());
		}

		return image;
	}

	public String getBase64Url(String image) {

		log.debug("Getting base64 image from {}", image);

		if (StringUtils.isNoneEmpty(image)) {
			if (!image.equals("https://s.gr-assets.com/assets/nophoto/user/u_200x266-e183445fd1a1b5cc7075bb1cf7043306.png")) {

				HttpURLConnection connection = null;
				try {
					connection = (HttpURLConnection) new URL(image).openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0");

					try (InputStream inputStream = connection.getInputStream();
						 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						byte[] buffer = new byte[4096];
						int bytesRead;
						while ((bytesRead = inputStream.read(buffer)) != -1) {
							byteArrayOutputStream.write(buffer, 0, bytesRead);
						}
						byte[] imageBytes = byteArrayOutputStream.toByteArray();

						Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));

						String rotate = String.valueOf(0);

						ExifIFD0Directory exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
						if (exifIFD0 != null) {
							int orientation = 0;
							try {
								orientation = exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION);
							}
							catch (MetadataException e) {
								log.error(e.getMessage());
							}

							switch (orientation) {
							case 1:
								rotate = String.valueOf(0);
								break;
							case 6:
								rotate = String.valueOf(90);
								break;
							case 3:
								rotate = String.valueOf(180);
								break;
							case 8:
								rotate = String.valueOf(270);
								break;
							}
						}
						BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
						image = getScaledImage(originalImage, Double.valueOf(rotate));
					}
				}
				catch (Exception e) {
					log.error(image + " --> " + e.getMessage());
					return null;
				}
				finally {
					if (connection != null) {
						connection.disconnect();
					}
				}

			}
		}

		return image;

	}

	private static String getScaledImage(BufferedImage originalImage, double rotate) throws IOException {

		int h = originalImage.getHeight();
		int w = originalImage.getWidth();

		if (h > w) {
			h = 250;
			w = (w * 250) / h;
		}
		else {
			w = 250;
			h = (h * 250) / w;
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Thumbnails.of(originalImage).size(w, h).outputFormat("jpg").rotate(rotate).toOutputStream(outputStream);

		return Base64.getEncoder().encodeToString(outputStream.toByteArray());
	}

	private static String getOriginalImage(BufferedImage originalImage) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(originalImage, "jpg", outputStream);
		return Base64.getEncoder().encodeToString(outputStream.toByteArray());
	}

	public Resource getEpub(String path) {
		Resource epub = null;

		try {
			String basePath;

			// Normalizar libraryPath para que termine con separador
			String normalizedLibraryPath = libraryPath;
			if (!normalizedLibraryPath.endsWith(File.separator)) {
				normalizedLibraryPath += File.separator;
			}

			// Verificar si el path es absoluto y ya contiene libraryPath
			File absoluteFile = new File(path);
			if (absoluteFile.isAbsolute()) {
				// Si el path es absoluto y está dentro de libraryPath, usarlo directamente
				String absolutePath = absoluteFile.getAbsolutePath();
				String normalizedLibraryPathNoSep = normalizedLibraryPath.substring(0, normalizedLibraryPath.length() - 1);

				if (absolutePath.startsWith(normalizedLibraryPathNoSep)) {
					// El path ya contiene libraryPath, usarlo tal cual
					basePath = absolutePath;
					log.debug("Using absolute path: {}", basePath);
				} else {
					// El path es absoluto pero no está dentro de libraryPath
					// Intentar extraer la parte relativa
					String relativePath = extractRelativePath(path, normalizedLibraryPath);
					basePath = normalizedLibraryPath + relativePath;
					log.debug("Extracted relative path from absolute: {} -> {}", path, basePath);
				}
			} else {
				// Path es relativo, concatenar con libraryPath
				String normalizedPath = path;
				while (normalizedPath.startsWith(File.separator)) {
					normalizedPath = normalizedPath.substring(1);
				}

				// Extraer el nombre del último directorio de libraryPath
				String libraryPathWithoutSeparator = libraryPath.endsWith(File.separator)
						? libraryPath.substring(0, libraryPath.length() - 1)
						: libraryPath;

				String lastDirOfLibrary = libraryPathWithoutSeparator.substring(
						libraryPathWithoutSeparator.lastIndexOf(File.separator) + 1
				);

				// Si normalizedPath comienza con el último directorio de libraryPath, eliminarlo
				if (normalizedPath.startsWith(lastDirOfLibrary + File.separator) ||
						normalizedPath.startsWith(lastDirOfLibrary)) {
					normalizedPath = normalizedPath.substring(lastDirOfLibrary.length());
					while (normalizedPath.startsWith(File.separator)) {
						normalizedPath = normalizedPath.substring(1);
					}
				}

				basePath = normalizedLibraryPath + normalizedPath;
				log.debug("Using relative path: {} -> {}", path, basePath);
			}

			File file = new File(basePath);
			log.debug("Checking path: {} (exists: {}, isDirectory: {})", basePath, file.exists(), file.isDirectory());

			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null) {
					for (File f : files) {
						if (f.getName().endsWith(".epub")) {
							epub = new UrlResource(f.toPath().toUri());
							log.info("Found epub file: {}", f.getAbsolutePath());
							break;
						}
					}
				}
				if (epub == null) {
					log.warn("No epub file found in directory: {}", basePath);
				}
			} else {
				log.warn("Directory does not exist or is not a directory: {}", basePath);
			}
		} catch (IOException e) {
			log.error("Error while getting epub from path: {}", path, e);
		}

		return epub;
	}

	private String extractRelativePath(String path, String libraryPath) {
		// Intentar extraer la parte relativa del path
		String normalizedPath = path;
		while (normalizedPath.startsWith(File.separator)) {
			normalizedPath = normalizedPath.substring(1);
		}

		String libraryPathWithoutSeparator = libraryPath.endsWith(File.separator)
				? libraryPath.substring(0, libraryPath.length() - 1)
				: libraryPath;

		String lastDirOfLibrary = libraryPathWithoutSeparator.substring(
				libraryPathWithoutSeparator.lastIndexOf(File.separator) + 1
		);

		if (normalizedPath.startsWith(lastDirOfLibrary + File.separator) ||
				normalizedPath.startsWith(lastDirOfLibrary)) {
			normalizedPath = normalizedPath.substring(lastDirOfLibrary.length());
			while (normalizedPath.startsWith(File.separator)) {
				normalizedPath = normalizedPath.substring(1);
			}
		}

		return normalizedPath;
	}

}
