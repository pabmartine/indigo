package com.martinia.indigo.book.application.resource;

import com.martinia.indigo.book.domain.ports.usecases.resource.ObtainBookByPathUseCase;
import com.martinia.indigo.common.util.ImageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class ObtainBookByPathUseCaseImpl implements ObtainBookByPathUseCase {

	@Resource
	private ImageUtils imageUtils;

	@Override
	public org.springframework.core.io.Resource getEpub(String path) {
		return imageUtils.getEpub(path.replace("@_@", "&").replace("@-@", "[").replace("@¡@", "]").replace("@!@", "`"));
	}

}
