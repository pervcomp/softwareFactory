package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.CommitAnalysis;

public interface CommitAnalysisDao extends MongoRepository<CommitAnalysis, String>{
	public List<CommitAnalysis> findbyidProject(String  idProject);
	public CommitAnalysis findCommitAnalysis(Sort sort);	
}
