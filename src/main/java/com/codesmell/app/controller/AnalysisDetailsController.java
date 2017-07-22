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

import com.codesmell.app.dao.AnalysisDetailsDao;
import com.codesmell.app.model.AnalysisDetails;

@Controller
@RequestMapping("/analysisDetails")
public class AnalysisDetailsController 
{
	private @Autowired AnalysisDetailsDao analysisDetailsDao;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<AnalysisDetails> getAllAnalysisDetials(@QueryParam("idProject") String idProject) 
	{
		return this.analysisDetailsDao.findByIdProject(idProject);
	}
	
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AnalysisDetails getAnalysisDetials(@PathVariable("id") String id) 
    {
    	return this.analysisDetailsDao.findBy_id(id);
    }
    
    
    @RequestMapping( method = RequestMethod.PUT)
    public AnalysisDetails saveProject(@RequestBody AnalysisDetails analysisDetails) 
    {
        this.analysisDetailsDao.save(analysisDetails);
        return analysisDetails;
    }
    
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AnalysisDetails deleteProject(@PathVariable("id") String id) 
    {
    	AnalysisDetails analysisDetails = this.analysisDetailsDao.findBy_id(id);
    	if(analysisDetails != null)
    	{
    		this.analysisDetailsDao.delete(analysisDetails);
    	}
    	return analysisDetails;
    }
}
