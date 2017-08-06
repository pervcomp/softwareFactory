package com.codesmell.app.dao;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;



public interface CommitDao extends MongoRepository<Commit, String>
{	
	public List<Commit>findByprojectName(String projectName);
	public Commit findBySsa(String ssa);
}
