package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.CommitAnalysis;

public interface CommitAnalysisDao extends MongoRepository<CommitAnalysis, String>{

	public List<CommitAnalysis> findbyidProject(String  idProject);
}
