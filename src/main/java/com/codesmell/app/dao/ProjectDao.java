package com.codesmell.app.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.Project;
import com.codesmell.app.model.User;

public interface ProjectDao extends MongoRepository<Project, String> 
{
	public List<Project> findByIdUser(Long  idUser);
	
	public Project findBy_id(String  idProject);
}
