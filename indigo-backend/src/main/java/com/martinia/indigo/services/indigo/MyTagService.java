package com.martinia.indigo.services.indigo;

import java.util.Optional;

import com.martinia.indigo.model.indigo.MyTag;

public interface MyTagService {

	Optional<MyTag> findById(int source);

	void image(int source, String image);

}
