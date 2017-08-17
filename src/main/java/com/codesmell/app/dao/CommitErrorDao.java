package com.codesmell.app.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;


public interface CommitErrorDao extends MongoRepository<CommitError, String> {
	public Project findByIdCommit(String  idCommit);
	public List<CommitError> findByEmailAndErrorDateBetweenOrderByErrorDateDesc(String email, Date start, Date end);
	public CommitError findByShaCommit(String shaCommit);
	
}
