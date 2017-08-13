package com.codesmell.app.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;


public interface ErrorDao extends MongoRepository<CommitError, String> {
	
	public Project findByIdCommit(String  idCommit);

}
