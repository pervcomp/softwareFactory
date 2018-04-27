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
	public String login(@ModelAttribute Commit commitDao, Model model, HttpServletRequest req, HttpServletResponse resp) 
	{
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
		CommitError commitError= this.commitErrorDao.findByShaCommit(commitDao.getSsa());
		String errMessage = "<html>"+commitError.getErrorMessage();
		errMessage = errMessage.replace("\n", "<br>");
        errMessage = errMessage.replace("\tat", "&nbsp&nbsp&nbsp");
        errMessage = errMessage.replace("\t", "&nbsp");
        errMessage += "</html>";
        String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
        commitError.setErrorMessage(errMessage);
	    model.addAttribute("commitError", commitError);
		return "stacktraceDetails";
	}
}
