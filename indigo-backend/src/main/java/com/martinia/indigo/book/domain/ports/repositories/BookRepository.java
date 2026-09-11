package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<BookMongoEntity, String>, CustomBookRepository {
	String INCOMPLETE_METADATA_QUERY = "{ '$and': ["
			+ "{ '$or': ["
			+ "{ 'title': { '$in': [null, ''] } },"
			+ "{ 'authors': { '$in': [null, []] } },"
			+ "{ 'comment': { '$in': [null, ''] } },"
			+ "{ 'pubDate': null },"
			+ "{ 'pages': { '$lte': 0 } },"
			+ "{ 'tags': { '$in': [null, []] } },"
			+ "{ 'image': { '$in': [null, ''] } },"
			+ "{ 'languages': { '$in': [null, []] } },"
			+ "{ '$and': ["
			+ "{ 'isbn10': { '$in': [null, []] } },"
			+ "{ 'isbn13': { '$in': [null, []] } },"
			+ "{ 'identifiers': { '$in': [null, {}] } }"
			+ "] }"
			+ "] },"
			+ "{ 'metadataMatchStatus': { '$ne': 'NO_MATCH' } }"
			+ "] }";

	@Query("{ 'serie.name' : ?0 }")
	List<BookMongoEntity> findBooksBySerie(String serie);

	@Query("{ 'tags' : ?0 }")
	List<BookMongoEntity> findByTag(String name);

	@Query("{ 'title' : ?0 }")
	Optional<BookMongoEntity> findByTitle(String title);

	@Query("{ 'path' : ?0 }")
	Optional<BookMongoEntity> findByPath(String path);

	@Query("{ '$or': [ { 'isbn10': { '$in': ?0 } }, { 'isbn13': { '$in': ?0 } } ] }")
	List<BookMongoEntity> findByAnyIsbn(java.util.Collection<String> isbn);
	List<BookMongoEntity> findByTitleIgnoreCase(String title);

	@Query("{ 'path' : { $in: ?0 } }")
	List<BookMongoEntity> findByPathIn(List<String> paths);

	@Query(value = INCOMPLETE_METADATA_QUERY, fields = "{ '_id': 1 }")
	List<BookMongoEntity> findBooksWithIncompleteMetadata();

	@Query(value = "{}", fields = "{ '_id': 1 }")
	List<BookMongoEntity> findAllBookIds();

	@Query(value = "{ '$or': [ { 'isbn10.0': { '$exists': true } }, { 'isbn13.0': { '$exists': true } } ] }",
			fields = "{ 'isbn10': 1, 'isbn13': 1 }")
	List<BookMongoEntity> findBooksWithIsbn();

}
