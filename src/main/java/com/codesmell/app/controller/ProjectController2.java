package com.codesmell.app.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.User;
import com.codesmell.app.sonar.SonarAnalysis;



@Controller
class ProjectController2 {
	
	@Autowired
	private ProjectDao projectDao;

	   @PostMapping("/createNewProject")
		public String createNewProject(Model model, @ModelAttribute Project project, HttpServletRequest req, HttpServletResponse resp) {
		    String emailSt = (String) req.getSession().getAttribute("email");
			model.addAttribute("email", emailSt);
		    project.setEmail("luca9294@hotmail.it");
		    if (projectDao.findByurl(project.getUrl()).length == 0)
		    		if (projectDao.findByprojectName(project.getName()).length == 0)
		    				projectDao.save(project);
		    model.addAttribute("projects",projectDao.findByemail(emailSt));
	    	    return "landingPage";
		}
	    
}
