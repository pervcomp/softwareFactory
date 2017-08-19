package com.codesmell.app.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.User;

import code.codesmell.app.controllerUtilities.ControllerUtilities;

@Controller
public class CommitErrorController {
	
	@Autowired private CommitErrorDao commitErrorDao;
	
	@PostMapping("/failureDetail")
	public String login(@ModelAttribute Commit commit) 
	{
		CommitError commitError= this.commitErrorDao.findByShaCommit(commit.getSsa());
		return "commitError";
	}
}
