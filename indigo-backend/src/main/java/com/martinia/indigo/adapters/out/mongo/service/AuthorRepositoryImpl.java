package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.AuthorMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.AuthorMongoRepository;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.ports.out.mongo.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AuthorRepositoryImpl implements AuthorRepository {

    @Autowired
    AuthorMongoRepository authorMongoRepository;

    @Autowired
    AuthorMongoMapper authorMongoMapper;

    @Override
    public Long count(List<String> languages) {
        return authorMongoRepository.count(languages);
    }

    @Override
    public List<Author> findAll(List<String> languages, int page, int size, String sort, String order) {

        List<AuthorMongoEntity> authors = authorMongoRepository.findAll(languages,
                PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort)));

        for (AuthorMongoEntity author : authors) {
            int total = 0;
            for (String key : author.getNumBooks()
                    .getLanguages()
                    .keySet()) {
                if (languages.contains(key)) {
                    total += author.getNumBooks()
                            .getLanguages()
                            .get(key);
                }
            }
            author.getNumBooks()
                    .setTotal(total);
        }

        return authorMongoMapper.entities2Domains(authors);
    }

    @Override
    public Optional<Author> findById(String id) {
        return authorMongoRepository.findById(id).map(author -> Optional.of(authorMongoMapper.entity2Domain(author))).orElse(Optional.empty());
    }

    public Optional<Author> findByName(String name) {
        return authorMongoRepository.findByName(name).map(author -> Optional.of(authorMongoMapper.entity2Domain(author))).orElse(Optional.empty());

    }

    public Optional<Author> findBySort(String sort) {
        return authorMongoRepository.findBySort(sort).map(author -> Optional.of(authorMongoMapper.entity2Domain(author))).orElse(Optional.empty());

    }

    @Override
    public void save(Author author) {

        AuthorMongoEntity mapping = authorMongoMapper.domain2Entity(author);
        Optional<AuthorMongoEntity> _entity = authorMongoRepository.findByName(author.getName());
        if (_entity.isPresent()) {
            AuthorMongoEntity entity = _entity.get();
            mapping.setId(entity.getId());
            mapping.getNumBooks()
                    .setTotal(entity.getNumBooks()
                            .getTotal() + 1);
            for (String key : entity.getNumBooks()
                    .getLanguages()
                    .keySet()) {
                if (mapping.getNumBooks()
                        .getLanguages()
                        .get(key) != null) {
                    mapping.getNumBooks()
                            .getLanguages()
                            .put(key, mapping.getNumBooks()
                                    .getLanguages()
                                    .get(key)
                                    + entity.getNumBooks()
                                    .getLanguages()
                                    .get(key));
                } else {
                    mapping.getNumBooks()
                            .getLanguages()
                            .put(key, entity.getNumBooks()
                                    .getLanguages()
                                    .get(key));
                }
            }
        }
        authorMongoRepository.save(mapping);

    }

    @Override
    public void update(Author author) {

        AuthorMongoEntity mapping = authorMongoMapper.domain2Entity(author);
        Optional<AuthorMongoEntity> entity = authorMongoRepository.findByName(author.getName());
        if (entity.isPresent()) {
            mapping.setId(entity.get().getId());
        }
        authorMongoRepository.save(mapping);

    }

    @Override
    public void dropCollection() {
        authorMongoRepository.dropCollection(AuthorMongoEntity.class);
    }

}
