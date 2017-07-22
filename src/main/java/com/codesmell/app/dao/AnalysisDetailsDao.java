package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.AnalysisDetails;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.User;

public interface AnalysisDetailsDao extends MongoRepository<AnalysisDetails, String>
{
	public List<AnalysisDetails> findByIdProject(String  idProjet);
}
