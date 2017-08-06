package com.codesmell.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;

@Controller
public class ProjectDetailsController {

	private @Autowired CommitAnalysisDao commitAnalysisDao;
	private @Autowired CommitDao commitDao;
	
	
	@PostMapping("/projectDetails")
    public String getProjectDetails(Model model, @ModelAttribute Project projectToSend) 
    {
		
    	List<CommitAnalysis> commitAnalysisList= this.commitAnalysisDao.findByIdProject(projectToSend.get_id());
    	List<CommitAnalysis> commitAnalysisList1= commitAnalysisList;
//    	try {
//        	List<Commit> commitList= this.commitDao.findByIdCommitAnalysis(commitAnalysis.get_id());
//        	
//        	ProjectDetails projectDetails= new ProjectDetails(project,commitAnalysis.getStartDate(),commitList);
//            model.addAttribute("projectsDetails", projectDetails);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}

        
        return "projectDetails";
    }
    
}