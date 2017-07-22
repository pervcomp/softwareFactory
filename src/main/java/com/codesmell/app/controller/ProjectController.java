package com.codesmell.app.controller;

import java.util.List;

import javax.ws.rs.QueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.model.Project;

@Controller
@RequestMapping("/project/")

public class ProjectController {

	private @Autowired ProjectDao projectDao;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseBody
	public List<Project> getAllProject(@QueryParam("idUser") Long idUser) 
	{
		return this.projectDao.findByIdUser(idUser);
	}
	
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Project getProject(@PathVariable("id") String id) 
    {
    	return this.projectDao.findBy_id(id);
    }
    
    
    @RequestMapping( method = RequestMethod.PUT)
    public Project saveProject(@RequestBody Project project) 
    {
        System.out.println("Creating Project " + project.getName());
        this.projectDao.save(project);
        return project;
    }
}
