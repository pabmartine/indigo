package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.adapters.out.mongo.entities.TagMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.TagMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.BookMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.TagMongoRepository;
import com.martinia.indigo.domain.model.Tag;
import com.martinia.indigo.ports.out.mongo.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Component
public class TagRepositoryImpl implements TagRepository {

    @Autowired
    TagMongoRepository tagMongoRepository;

    @Autowired
    BookMongoRepository bookMongoRepository;

    @Autowired
    TagMongoMapper tagMongoMapper;

    @Override
    public List<Tag> findAll(List<String> languages, String sort, String order) {

        List<TagMongoEntity> tags = tagMongoRepository.findAll(languages,
                Sort.by(Sort.Direction.fromString(order), sort));

        for (TagMongoEntity tag : tags) {
            int total = 0;
            for (String key : tag.getNumBooks()
                    .getLanguages()
                    .keySet()) {
                if (languages.contains(key)) {
                    total += tag.getNumBooks()
                            .getLanguages()
                            .get(key);
                }
            }
            tag.getNumBooks()
                    .setTotal(total);
        }

        return tagMongoMapper.entities2Domains(tags);
    }

    @Override
    public Optional<Tag> findByName(String name) {
        return tagMongoRepository.findByName(name).map(tag -> Optional.of(tagMongoMapper.entity2Domain(tag))).orElse(Optional.empty());
    }

    @Override
    public void image(String source, String image) {
        TagMongoEntity entity = tagMongoRepository.findById(source)
                .get();
        entity.setImage(image);
        tagMongoRepository.save(entity);
    }

    @Override
    public void merge(String source, String target) {
        TagMongoEntity entitySource = tagMongoRepository.findById(source)
                .get();
        TagMongoEntity entityTarget = tagMongoRepository.findById(target)
                .get();

        List<BookMongoEntity> books = bookMongoRepository.findByTag(entitySource.getName());
        for (BookMongoEntity book : books) {
            book.getTags()
                    .replaceAll(b -> b.equals(entitySource.getName()) ? entityTarget.getName() : b);
            book.setTags(new ArrayList<>(new LinkedHashSet<>(book.getTags())));
        }
        bookMongoRepository.saveAll(books);

        entityTarget.getNumBooks()
                .setTotal(entityTarget.getNumBooks()
                        .getTotal()
                        + entitySource.getNumBooks()
                        .getTotal());
        for (String lang : entitySource.getNumBooks()
                .getLanguages()
                .keySet()) {
            if (entityTarget.getNumBooks()
                    .getLanguages()
                    .get(lang) != null) {
                entityTarget.getNumBooks()
                        .getLanguages()
                        .put(lang, entityTarget.getNumBooks()
                                .getLanguages()
                                .get(lang)
                                + entitySource.getNumBooks()
                                .getLanguages()
                                .get(lang));
            } else {
                entityTarget.getNumBooks()
                        .getLanguages()
                        .put(lang, entitySource.getNumBooks()
                                .getLanguages()
                                .get(lang));
            }
        }
        tagMongoRepository.save(entityTarget);
        tagMongoRepository.delete(entitySource);

    }

    @Override
    public void rename(String source, String target) {

        TagMongoEntity entity = tagMongoRepository.findById(source)
                .get();

        List<BookMongoEntity> books = bookMongoRepository.findByTag(entity.getName());
        for (BookMongoEntity book : books) {
            book.getTags()
                    .replaceAll(b -> b.equals(entity.getName()) ? target : b);
        }
        bookMongoRepository.saveAll(books);

        entity.setName(target);
        tagMongoRepository.save(entity);

    }

    @Override
    public void save(List<String> tags, List<String> languages) {
        tags.forEach(tag -> {
            Optional<TagMongoEntity> _entity = tagMongoRepository.findByName(tag);
            if (_entity.isEmpty()) {
                tagMongoRepository.save(new TagMongoEntity(tag, languages));
            } else {
                TagMongoEntity entity = _entity.get();
                languages.forEach(lang -> {
                    if (entity.getNumBooks()
                            .getLanguages()
                            .get(lang) != null) {
                        entity.getNumBooks()
                                .getLanguages()
                                .put(lang, entity.getNumBooks()
                                        .getLanguages()
                                        .get(lang) + 1);
                    } else {
                        entity.getNumBooks()
                                .getLanguages()
                                .put(lang, 1);
                    }
                });
                entity.getNumBooks()
                        .setTotal(entity.getNumBooks()
                                .getTotal() + 1);
                tagMongoRepository.save(entity);
            }
        });

    }

    @Override
    public void dropCollection() {
        tagMongoRepository.dropCollection(TagMongoEntity.class);
    }

}
