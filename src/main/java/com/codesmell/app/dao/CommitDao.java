package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.Commit;

public interface CommitDao extends MongoRepository<Commit, String>
{	
	public List<Commit>findByprojectName(String projectName);
	public Commit findBySsa(String ssa);
	public List<Commit> findByProjectNameOrderByCreationDateDesc(String projectName);

}
