package com.codesmell.app.dao;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;

public interface CommitAnalysisDao extends MongoRepository<CommitAnalysis, String>{
	public CommitAnalysis findBy_id(String  idAnalysis);
	
}
