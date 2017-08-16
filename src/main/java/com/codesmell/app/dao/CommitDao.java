package com.codesmell.app.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.Commit;

public interface CommitDao extends MongoRepository<Commit, String>
{	
	public List<Commit>findByProjectName(String projectName);
	public Commit findBySsa(String ssa);
	public List<Commit> findByProjectNameOrderByCreationDateDesc(String projectName);
	public List<Commit> findByProjectNameAndStatus(String projectName, String status);
	public List<Commit> findByAnalysisDateGreaterThanAndProjectNameAndStatus(Date analysisDate, String projectName,String status);
	public List<Commit> findByAnalysisDateGreaterThanAndProjectName(Date analysisDate, String projectName);

}
