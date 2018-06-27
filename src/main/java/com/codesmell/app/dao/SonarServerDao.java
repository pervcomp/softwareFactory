package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.SonarServer;
import com.codesmell.app.model.User;

public interface SonarServerDao extends MongoRepository<SonarServer, String> {
    public List<SonarServer> findAllByOrderBySonarDateDesc();
}