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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

		bookOpf.setBookImage(extractImage(path, Optional.ofNullable(bookOpf.getBookImageName()).orElse("cover.jpg")));
		bookOpf.setAuthorImage(extractImage(path, Optional.ofNullable(bookOpf.getAuthorImageName()).orElse("autor.jpg")));

		eventBus.publish(EpubFileExtractedEvent.builder().path(path).bookOpf(bookOpf).build());

	}

	private Path checkEpubPath(final Path path) {
		final String strPath = path.toString();
		final String fileName = path.getFileName().toString();
		final String basePath = strPath.replace(fileName, "").replace("/", "");
		if (basePath.trim().equals(uploadsPath.replace("/", "").trim())) {
			try {
				Path newPath = Files.createDirectory(Path.of(uploadsPath + FileSystems.getDefault().getSeparator() + UUID.randomUUID()));
				Path movedPath = Files.move(path, newPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
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

	private String extractImage(final Path path, String fileName) {
		String image = null;
		String realFileName = fileName;
		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(path.toFile()))) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName().toLowerCase().contains(fileName.toLowerCase())) {
					realFileName = zipEntry.getName();
					createImageIfNotExist(zipInputStream, path, zipEntry.getName().toLowerCase());
					image = imageUtils.getBase64Cover(zipInputStream, true);
					break;
				}
				zipEntry = zipInputStream.getNextEntry();
			}
		}
		catch (Exception ex) {
			log.error(ex.getMessage());
			image = findAlternativeImageInPath(path, realFileName);
		}
		return image;
	}

	private String findAlternativeImageInPath(final Path path, final String fileName) {
		String image = null;
		try {
			final Path sourceCoverPath = Path.of(path.getParent() + FileSystems.getDefault().getSeparator() + fileName.replace("jpeg", "jpg"));
			if (sourceCoverPath.toFile().exists()) {
				image = imageUtils.getBase64Cover(Files.newInputStream(sourceCoverPath), true);
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return image;
	}

	private void createImageIfNotExist(final InputStream inputStream, final Path path, final String fileName) {
		try {
			final Path sourceCoverPath = Path.of(path.getParent() + FileSystems.getDefault().getSeparator() + fileName.replace("jpeg", "jpg"));
			if (!sourceCoverPath.toFile().exists()) {
				Files.copy(inputStream, sourceCoverPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}


