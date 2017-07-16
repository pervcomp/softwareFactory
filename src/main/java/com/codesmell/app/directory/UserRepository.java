package com.codesmell.app.directory;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    public User findByEmail1(String email1);
}