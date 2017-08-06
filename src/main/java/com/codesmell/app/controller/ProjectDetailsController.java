package com.codesmell.app.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.ProjectDetails;
import com.codesmell.app.model.User;
import com.codesmell.app.sonar.SonarAnalysis;

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