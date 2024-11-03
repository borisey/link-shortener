package com.borisey.link_shortener.repo;

import com.borisey.link_shortener.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUUID(String UUID);
}
