package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.Commit;
import com.codesmell.app.model.Project;


public interface ProjectDao extends MongoRepository<Project, String> 
{
	public List<Project> findByIdUser(String  idUser);
	
	public Project findBy_id(String  idProject);
	
	public Project[] findByurl(String  url);
	
	public Project findByprojectName(String  projectName);
	
	public List<Project> findByemail(String  email);
	

}
