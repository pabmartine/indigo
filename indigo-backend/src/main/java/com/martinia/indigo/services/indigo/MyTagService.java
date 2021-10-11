package com.martinia.indigo.services.indigo;

import java.util.Optional;

import com.martinia.indigo.model.indigo.MyTag;

public interface MyTagService {

	void save(MyTag tag);

	Optional<MyTag> findById(int source);

}
