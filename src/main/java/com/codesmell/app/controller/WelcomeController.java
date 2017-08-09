package com.codesmell.app.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.Schedule;
import com.codesmell.app.model.User;

@Controller
class WelcomeController {

	@Autowired
	private UserDao repository;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private CommitDao commitDao;


	@RequestMapping("/")
	public String welcome(@CookieValue(value = "email", defaultValue = "") String email, Model model,
			HttpServletRequest req, HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") != null) {
			String emailSession = (String) req.getSession().getAttribute("email");
			configureModelLandingPage(model, emailSession);
			return "landingPage";
		} else if (!email.isEmpty()) {
			req.getSession().setAttribute("email", email);
			configureModelLandingPage(model, email);
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
			String emailSession = (String) req.getSession().getAttribute("email");
			configureModelLandingPage(model, emailSession);
			return "landingPage";
		}
	}
	
	@PostMapping("/projectDet")
	public String projectDetails(Model model, @ModelAttribute Project projectToSend,HttpServletRequest req, HttpServletResponse resp) {
		String projectName = projectToSend.getProjectName();
		Project project = projectDao.findByprojectName(projectName)[0];
		getUpdateProject(project);
		List<CommitAnalysis> analysis = commitAnalysisDao.findByIdProject(project.getProjectName());
		List<Commit> commits = commitDao.findByProjectNameOrderByCreationDateDesc(project.getProjectName());
		model.addAttribute("commits", commits);
		if (!analysis.isEmpty())
				model.addAttribute("analysis", analysis.get(analysis.size()-1));
		model.addAttribute("project", project);
		model.addAttribute("email", req.getSession().getAttribute("email"));
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
			User usr = repository.findByEmail1(emailSt);
			System.out.println(usr.getPwd());
			System.out.println(usr.getEmail1());
			if (pwd.equals((usr.getPwd()))) {
				req.getSession().setAttribute("email", emailSt);
				resp.addCookie(new Cookie("email", emailSt));
				configureModelLandingPage(model, emailSt);
				return "landingPage";
			} else {
				return "welcome";
			}
		} catch (Exception e) {
			return "welcome";
		}
	}

	private List<Project> getProjects(String email) {
		List<Project> projects = projectDao.findByemail(email);
		for (Project p : projects) {
			getUpdateProject(p);
		}
		return projects;
	}
	
	private void getUpdateProject(Project p){
		List<CommitAnalysis> analysis = commitAnalysisDao.findByIdProject(p.getProjectName());
		String url = p.getUrl();
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		if (p.getTotalCommits() == 0
				|| (((new Date().getTime() - p.getLastRequest().getTime()) / 1000 / 3600) > 6)) {
			int count = getCommitsCount(url);
			System.out.println("RENEW AMOUNT");
			p.setTotalCommits(count);
			p.setLastRequest(new Date());
		}
		if (analysis.size() >0){
		Date analysisDate = new Date();
		
		if (analysis.get(analysis.size()-1).getStatus() == "Processing")
			analysisDate = analysis.get(analysis.size()-1).getStartDate();
		else
			analysisDate = analysis.get(analysis.size()-1).getEndDate();
		
		

		p.setLastAnalysis(analysisDate);
		p.setStatus(analysis.get(analysis.size()-1).getStatus());
		}
		p.setAnalysedCommits(commitDao.findByprojectName(p.getProjectName()).size());
		p.setCountFailedCommits((commitDao.findByProjectNameAndStatus(p.getProjectName(), "FAILED").size()));
		p.setCountSuccessCommits((commitDao.findByProjectNameAndStatus(p.getProjectName(), "SUCCESS").size()));
		projectDao.save(p);
	}
	

	private int getCommitsCount(String url) {
		int count = 0;
		File d = new File("directory");
		Git git;
		try {
			git = Git.cloneRepository().setURI(url).setDirectory(d).call();
			Iterable<RevCommit> commits = git.log().call();
			for (RevCommit commit : commits)
				count++;
			FileUtils.deleteDirectory(d);
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return count;
	}

	private void configureModelLandingPage(Model model, String email) {
		model.addAttribute("projects", getProjects(email));
		model.addAttribute("email", email);
		model.addAttribute("projectToSend", new Project());
	}

}
