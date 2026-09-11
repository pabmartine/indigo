package com.martinia.indigo.file.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.common.util.DateUtils;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.common.util.XmlUtils;
import com.martinia.indigo.file.domain.model.events.EpubFileExtractedEvent;
import com.martinia.indigo.file.domain.ports.usecases.commands.ExtractEpubFileUseCase;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Transactional
@Slf4j
public class ExtractEpubFileUseCaseImpl implements ExtractEpubFileUseCase {

	@Value("${book.library.uploads}")
	private String uploadsPath;

	@Resource
	private ImageUtils imageUtils;
	@Resource
	private BookMongoMapper bookMongoMapper;
	@Resource
	private BookRepository bookRepository;
	@Resource
	private TagRepository tagRepository;
	@Resource
	private DateUtils dateUtils;
	@Resource
	private EventBus eventBus;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	@Transactional
	public void extract(Path path) {

		path = checkEpubPath(path);

		final BookOpf bookOpf = extractEpub(path);
		if (bookOpf == null) {
			log.error("No se encontró el archivo .opf en el EPUB");
			uploadEpubFilesSingleton.addExtractError();
			return;
		}
		else {
			uploadEpubFilesSingleton.addExtract();
		}

		bookOpf.setBookImage(extractBookCover(path, Optional.ofNullable(bookOpf.getBookImageName()).orElse("cover.jpg")));
		bookOpf.setAuthorImage(extractImage(path, Optional.ofNullable(bookOpf.getAuthorImageName()).orElse("autor.jpg"), null));

		eventBus.publish(EpubFileExtractedEvent.builder().path(path).bookOpf(bookOpf).build());

	}

	private Path checkEpubPath(final Path path) {
		final Path normalizedPath = path.toAbsolutePath().normalize();
		final Path uploadsRoot = Path.of(uploadsPath).toAbsolutePath().normalize();
		final String fileName = normalizedPath.getFileName().toString();
		if (uploadsRoot.equals(normalizedPath.getParent())) {
			try {
				Path newPath = Files.createDirectory(uploadsRoot.resolve(UUID.randomUUID().toString()));
				Path movedPath = Files.move(normalizedPath, newPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
				return movedPath;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return path;
	}

	private BookOpf extractEpub(final Path path) {
		BookOpf bookOpf = null;
		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(path.toFile()))) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName().endsWith(".opf")) {
					bookOpf = XmlUtils.parse(zipInputStream);
					break;
				}

				zipEntry = zipInputStream.getNextEntry();
			}
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
		return bookOpf;
	}

	private String extractImage(final Path path, String fileName, final String destinationFileName) {
		String image = null;
		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(path.toFile()))) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName().toLowerCase().contains(fileName.toLowerCase())) {
					final byte[] imageBytes = zipInputStream.readAllBytes();
					if (destinationFileName != null) {
						createImageIfNotExist(imageBytes, path, destinationFileName);
					}
					image = imageUtils.getBase64Cover(new ByteArrayInputStream(imageBytes), true);
					break;
				}
				zipEntry = zipInputStream.getNextEntry();
			}
		}
		catch (Exception ex) {
			log.error("Unable to extract EPUB image from {}", path, ex);
			if (destinationFileName != null) {
				image = findAlternativeImageInPath(path, destinationFileName);
			}
		}
		return image;
	}

	private String extractBookCover(final Path path, final String fileName) {
		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(path.toFile()))) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName().toLowerCase().contains(fileName.toLowerCase())) {
					return imageUtils.saveCoverAndGetThumbnail(zipInputStream.readAllBytes(), path.getParent().resolve("cover.jpg"));
				}
				zipEntry = zipInputStream.getNextEntry();
			}
		}
		catch (Exception ex) {
			log.error("Unable to extract EPUB cover from {}", path, ex);
			return findAlternativeImageInPath(path, "cover.jpg");
		}
		return null;
	}

	private String findAlternativeImageInPath(final Path path, final String fileName) {
		String image = null;
		try {
			final Path sourceCoverPath = Path.of(path.getParent() + FileSystems.getDefault().getSeparator() + fileName.replace("jpeg", "jpg"));
			if (sourceCoverPath.toFile().exists()) {
				try (var inputStream = Files.newInputStream(sourceCoverPath)) {
					image = imageUtils.getBase64Cover(inputStream, true);
				}
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return image;
	}

	private void createImageIfNotExist(final byte[] imageBytes, final Path path, final String fileName) {
		try {
			final Path sourceCoverPath = path.getParent().resolve(fileName).normalize();
			if (!sourceCoverPath.startsWith(path.getParent().toAbsolutePath().normalize())) {
				throw new IllegalArgumentException("Invalid EPUB image path");
			}
			if (!sourceCoverPath.toFile().exists()) {
				Files.write(sourceCoverPath, imageBytes);
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
