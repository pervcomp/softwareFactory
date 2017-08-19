package com.codesmell.app.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.ScheduleDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.Schedule;
import com.codesmell.app.model.User;

import code.codesmell.app.controllerUtilities.ControllerUtilities;

@Controller
class WelcomeController {

	@Autowired
	private UserDao userDao;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private CommitDao commitDao;
	@Autowired
	private ScheduleDao scheduleDao;
	@Autowired
	private CommitErrorDao commitErrorDao;
	@Autowired
	private JavaMailSender mailSender;


	@RequestMapping("/")
	public String welcome(@CookieValue(value = "email", defaultValue = "") String email, Model model,
			HttpServletRequest req, HttpServletResponse resp) {
	    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
		if (req.getSession().getAttribute("email") != null) {
			String emailSession = (String) req.getSession().getAttribute("email");
			cu.configureModelLandingPage(model, emailSession);
			cu.scheduleDailyReport(userDao.findByEmail1((String)req.getSession().getAttribute("email")), mailSender);
			return "landingPage";
		} else if (!email.isEmpty()) {
			req.getSession().setAttribute("email", email);
			cu.configureModelLandingPage(model, email);
			cu.scheduleDailyReport(userDao.findByEmail1((String)req.getSession().getAttribute("email")), mailSender);
			return "landingPage";
		}
		model.addAttribute("user", new User());
		return "welcome";
	}

	@RequestMapping("/newproject")
	public String newProject(Model model, HttpServletRequest req, HttpServletResponse resp) {
		model.addAttribute("email", req.getSession().getAttribute("email"));
		model.addAttribute("project", new Project());
		model.addAttribute("schedule", new Schedule());
		return "newproject";
	}

	@RequestMapping("/landingPage")
	public String landingPaget(Model model, HttpServletRequest req, HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") == null)
			return "welcome";
		else {
		    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
			String emailSession = (String) req.getSession().getAttribute("email");
			cu.configureModelLandingPage(model, emailSession);
			cu.scheduleDailyReport(userDao.findByEmail1((String)req.getSession().getAttribute("email")), mailSender);
			return "landingPage";
		}
	}
	
	/**
	 * Prepares the projectDetails page
	 * @param model
	 * @param projectToSend
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/projectDet")
	public String projectDetails(Model model, @ModelAttribute Project projectToSend,HttpServletRequest req, HttpServletResponse resp) {
	    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
		String projectName = projectToSend.getProjectName();
		Project p = projectDao.findByprojectName(projectName);
		cu.getUpdateProject(p);		
		cu.configureModelDetailsPage(model,(String)req.getSession().getAttribute("email"),p);
		return "projectDetails";
	}
	
	@RequestMapping("/logout")
	public String logout(Model model, HttpServletRequest req, HttpServletResponse resp) {
		req.getSession().removeAttribute("" + "email");
		Cookie cookie = new Cookie("email", null); // Not necessary, but saves
													// bandwidth.
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0); // Don't set to -1 or it will become a session
								// cookie!
		resp.addCookie(cookie);
		model.addAttribute("user", new User());
	
		return "welcome";
	}

	@PostMapping("/login")
	public String login(Model model, @ModelAttribute User user, HttpServletRequest req, HttpServletResponse resp) {
		try {
			String emailSt = user.getEmail1();
			String pwd = user.getPwd();
			User usr = userDao.findByEmail1(emailSt);
			if (pwd.equals((usr.getPwd()))) {
			    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
				req.getSession().setAttribute("email", emailSt);
				resp.addCookie(new Cookie("email", emailSt));
				cu.configureModelLandingPage(model, emailSt);
				cu.scheduleDailyReport(userDao.findByEmail1((String)req.getSession().getAttribute("email")), mailSender);
				return "landingPage";
			} else {
				return "welcome";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "welcome";
		}
	}

}
