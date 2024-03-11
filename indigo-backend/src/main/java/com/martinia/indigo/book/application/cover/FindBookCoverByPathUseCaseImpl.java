package com.martinia.indigo.book.application.cover;

import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByPathUseCase;
import com.martinia.indigo.common.util.ImageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class FindBookCoverByPathUseCaseImpl implements FindBookCoverByPathUseCase {

	@Resource
	private ImageUtils imageUtils;

	@Override
	public Optional<String> getImage(String path) {
		return Optional.ofNullable(imageUtils.getBase64Cover(path, false));
	}
}
