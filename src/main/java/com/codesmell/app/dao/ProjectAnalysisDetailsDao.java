package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.ProjetcAnalysisDetails;

public interface ProjectAnalysisDetailsDao extends MongoRepository<ProjetcAnalysisDetails, String>
{
	public List<ProjetcAnalysisDetails> findByIdProject(String  idProjet);
	
	public ProjetcAnalysisDetails findBy_id(String  idProject);
}
