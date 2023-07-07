package com.martinia.indigo.book.application.cover;

import com.martinia.indigo.book.domain.service.cover.FindBookCoverByPathUseCase;
import com.martinia.indigo.common.util.UtilComponent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class FindBookCoverByPathUseCaseImpl implements FindBookCoverByPathUseCase {

	@Resource
	private UtilComponent utilComponent;

	@Override
	public Optional<String> getImage(String path) {
		return Optional.ofNullable(utilComponent.getBase64Cover(path, false));
	}
}
