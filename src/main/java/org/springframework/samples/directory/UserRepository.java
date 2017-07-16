package org.springframework.samples.directory;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.sample.module.User;

public interface UserRepository extends MongoRepository<User, String> {
    public User findByEmail1(String email1);
}