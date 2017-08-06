package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.CommitAnalysis;

public interface CommitAnalysisDao extends MongoRepository<CommitAnalysis, String>{
	@Query("select * from CommitAnalysis u where u.idProject = ?1 and u.status = ?2  FETCH 1 ROWS ONLY")
	public CommitAnalysis findByIdProjectAndStatus(String  idProject, String status);
	public List<CommitAnalysis> findByIdProject(String  idProject);
	public CommitAnalysis findBy_id(String analysis);
}
