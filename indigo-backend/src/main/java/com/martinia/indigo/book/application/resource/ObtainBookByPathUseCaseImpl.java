package com.martinia.indigo.book.application.resource;

import com.martinia.indigo.book.domain.service.resource.ObtainBookByPathUseCase;
import com.martinia.indigo.domain.util.UtilComponent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class ObtainBookByPathUseCaseImpl implements ObtainBookByPathUseCase {

	@Resource
	private UtilComponent utilComponent;

	@Override
	public org.springframework.core.io.Resource getEpub(String path) {
		return utilComponent.getEpub(path.replace("@_@", "&").replace("@-@", "[").replace("@¡@", "]").replace("@!@", "`"));
	}

}
