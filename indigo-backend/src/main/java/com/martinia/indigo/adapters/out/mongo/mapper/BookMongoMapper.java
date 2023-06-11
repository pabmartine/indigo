package com.martinia.indigo.adapters.out.mongo.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.domain.model.Book;

@Mapper(componentModel = "spring")
public interface BookMongoMapper {

	BookMongoEntity domain2Entity(Book domain);

	Book entity2Domain(BookMongoEntity entity);

	List<BookMongoEntity> domain2Entity(List<Book> domains);

	List<Book> entities2Domains(List<BookMongoEntity> entities);

}