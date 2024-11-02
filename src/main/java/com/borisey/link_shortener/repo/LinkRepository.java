package com.borisey.link_shortener.repo;

import com.borisey.link_shortener.models.Link;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

public interface LinkRepository extends CrudRepository<Link, Long> {
    Iterable<Link> findAll(Sort colName);
}
