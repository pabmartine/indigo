package com.martinia.indigo.repository.indigo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

	User findByUsername(String username);
	
	List<User> findAll();

}
