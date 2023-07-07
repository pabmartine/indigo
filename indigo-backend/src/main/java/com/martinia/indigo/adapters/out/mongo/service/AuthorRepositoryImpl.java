package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.AuthorMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.AuthorMongoRepository;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        authors = authors.stream().map(author -> {
            author.getNumBooks().setTotal(author.getNumBooks().getLanguages().keySet().stream().filter(lang -> languages.contains(lang)).mapToInt(lang -> author.getNumBooks().getLanguages().get(lang)).sum());
            if (author.getImage() != null && author.getImage().equals("https://s.gr-assets.com/assets/nophoto/user/u_200x266-e183445fd1a1b5cc7075bb1cf7043306.png")) {
                author.setImage(null);
            }
            return author;
        }).collect(Collectors.toList());


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
