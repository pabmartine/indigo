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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
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
			String basePath = path.startsWith(normalizeLibraryPath()) ? path : normalizeLibraryPath() + path;
			if (!isInsideLibrary(basePath)) {
				log.warn("Refusing to read cover outside the configured library: {}", path);
				return null;
			}

			File file = new File(basePath);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null) {
					for (File f : files) {
						if (f.getName().endsWith(".epub")) {

							try (ZipFile zipFile = new ZipFile(f)) {
								Enumeration<? extends ZipEntry> zipFiles = zipFile.entries();

								while (zipFiles.hasMoreElements() && image == null) {
									ZipEntry entry = zipFiles.nextElement();
									if (!entry.isDirectory()) {

										String fileName = entry.getName().toLowerCase();
										int lastDot = fileName.lastIndexOf(".");
										String extension = lastDot >= 0 ? fileName.substring(lastDot + 1) : "";

										for (String type : types) {
											if (fileName.contains(type) && Arrays.asList("jpg", "jpeg", "png").contains(extension)) {
												try (InputStream is = zipFile.getInputStream(entry)) {
													BufferedImage originalImage = ImageIO.read(is);
													if (originalImage != null) {
														image = getScaledImage(originalImage, 0);
														break;
													}
												}
											}
										}

									}

								}
							}

						}
						if (image != null) {
							break;
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.debug("Error extracting image from epub {}", path, e);
		}
		return image;
	}

	public String getBase64Cover(String path, boolean scale) {
		String image = null;
		String fullPath = path.startsWith(normalizeLibraryPath()) ? path : normalizeLibraryPath() + path;

		if (isInsideLibrary(fullPath) && (new File(fullPath)).exists()) {
			try {
				String coverPath = fullPath + "/cover.jpg";

				File coverFile = new File(coverPath);
				if (coverFile.exists()) {
					BufferedImage originalImage = ImageIO.read(coverFile);
					if (originalImage != null) {
						if (scale) {
							image = getScaledImage(originalImage, 0);
						}
						else {
							image = getOriginalImage(originalImage);
						}
					}
				}
			}
			catch (Exception e) {
				log.debug(e.getMessage());
			}
		}

		if (image == null) {
			image = getImageFromEpub(fullPath, "cover", "thumbnail");
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

	/**
	 * Stores an imported cover as a real JPEG and returns the thumbnail used by book listings.
	 */
	public String saveCoverAndGetThumbnail(final byte[] imageBytes, final Path coverPath) throws IOException {
		final BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
		if (originalImage == null) {
			return null;
		}
		final BufferedImage jpegImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics = jpegImage.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, jpegImage.getWidth(), jpegImage.getHeight());
			graphics.drawImage(originalImage, 0, 0, null);
		}
		finally {
			graphics.dispose();
		}
		Files.createDirectories(coverPath.getParent());
		ImageIO.write(jpegImage, "jpg", coverPath.toFile());
		return getScaledImage(jpegImage, 0);
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

		int originalHeight = originalImage.getHeight();
		int originalWidth = originalImage.getWidth();
		int h;
		int w;

		if (originalHeight > originalWidth) {
			h = 250;
			w = Math.max(1, (originalWidth * 250) / originalHeight);
		}
		else {
			w = 250;
			h = Math.max(1, (originalHeight * 250) / originalWidth);
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
			if (!isInsideLibrary(basePath)) {
				log.warn("Refusing to read EPUB outside the configured library: {}", path);
				return null;
			}
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

	private String normalizeLibraryPath() {
		return libraryPath.endsWith(File.separator) ? libraryPath : libraryPath + File.separator;
	}

	private boolean isInsideLibrary(String path) {
		Path libraryRoot = Path.of(libraryPath).toAbsolutePath().normalize();
		Path requestedPath = Path.of(path).toAbsolutePath().normalize();
		return requestedPath.startsWith(libraryRoot);
	}

}
