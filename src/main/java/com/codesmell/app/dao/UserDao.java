package com.codesmell.app.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.User;

public interface UserDao extends MongoRepository<User, String> {
    public User findByEmail1(String email1);
}