package com.borisey.link_shortener.repo;

import com.borisey.link_shortener.models.Link;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

public interface LinkRepository extends CrudRepository<Link, Long> {
    Iterable<Link> findAll(Sort colName);
    Link findByShortUrl(String shortUrl);
    Iterable<Link> findByUUID(String UUID);
    Link findByIdAndUUID(Long id, String UUID);
}
