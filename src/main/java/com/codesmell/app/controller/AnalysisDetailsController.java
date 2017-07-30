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

import com.codesmell.app.dao.ProjectAnalysisDetailsDao;
import com.codesmell.app.model.ProjetcAnalysisDetails;

@Controller
@RequestMapping("/analysisDetails")
public class AnalysisDetailsController 
{
	private @Autowired ProjectAnalysisDetailsDao analysisDetailsDao;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<ProjetcAnalysisDetails> getAllAnalysisDetials(@QueryParam("idProject") String idProject) 
	{
		return this.analysisDetailsDao.findByIdProject(idProject);
	}
	
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjetcAnalysisDetails getAnalysisDetials(@PathVariable("id") String id) 
    {
    	return this.analysisDetailsDao.findBy_id(id);
    }
    
    
    @RequestMapping( method = RequestMethod.PUT)
    public ProjetcAnalysisDetails saveProject(@RequestBody ProjetcAnalysisDetails analysisDetails) 
    {
        this.analysisDetailsDao.save(analysisDetails);
        return analysisDetails;
    }
    
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjetcAnalysisDetails deleteProject(@PathVariable("id") String id) 
    {
    	ProjetcAnalysisDetails analysisDetails = this.analysisDetailsDao.findBy_id(id);
    	if(analysisDetails != null)
    	{
    		this.analysisDetailsDao.delete(analysisDetails);
    	}
    	return analysisDetails;
    }
}
