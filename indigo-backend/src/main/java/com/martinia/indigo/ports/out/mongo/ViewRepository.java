package com.martinia.indigo.ports.out.mongo;

import com.martinia.indigo.common.model.View;

public interface ViewRepository {

	void save(View notification);

}
