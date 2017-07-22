package com.codesmell.app.controller;

import java.util.List;
import javax.ws.rs.QueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.model.Project;


@Controller
@RequestMapping("/project")

public class ProjectController {

	private @Autowired ProjectDao projectDao;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String getAllProject(@QueryParam("idUser") String idUser,Model model) 
	{
		List<Project> projectsList= this.projectDao.findByIdUser(idUser);
		model.addAttribute("projects", projectsList);
		
		return "projectDetails";
		
	}
	
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getProject(@PathVariable("id") String id,Model model) 
    {
    	Project project= this.projectDao.findBy_id(id);
        model.addAttribute("projects", project);
        
        return "projectDetails";
    }
    
    
    @RequestMapping( method = RequestMethod.PUT)
    public String saveProject(@RequestBody Project project,Model model) 
    {
        Project savedProject= this.projectDao.save(project);
        model.addAttribute("projects", savedProject);
        
        return "projectDetails";
    }
    
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String deleteProject(@PathVariable("id") String id,Model model) 
    {
    	Project project = this.projectDao.findBy_id(id);
    	if(project != null)
    	{ model.addAttribute("projects", project);
    		this.projectDao.delete(project);
    	}
    	return "projectDetails";
    }
    
}
