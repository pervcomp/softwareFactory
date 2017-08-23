package com.codesmell.app.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codesmell.app.model.Project;
import com.codesmell.app.model.Schedule;;

public interface ScheduleDao extends MongoRepository<Schedule, String>  {
	
	public  Schedule findByProjectName (String  projectName);
	public  void deleteByProjectName (String  projectName);
}
