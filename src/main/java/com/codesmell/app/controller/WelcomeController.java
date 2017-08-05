package com.codesmell.app.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.aspectj.weaver.Iterators;
import org.assertj.core.internal.Iterables;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
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
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.User;
import com.codesmell.app.sonar.SonarAnalysis;



@Controller
class WelcomeController {

	@Autowired
	private UserDao repository;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	SonarAnalysis so;

	@RequestMapping("/")
	public String welcome(@CookieValue(value = "email", defaultValue = "") String email, Model model,
			HttpServletRequest req, HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") != null) {
			model.addAttribute("projectToSend", new Project());
			model.addAttribute("email", req.getSession().getAttribute("email"));
			model.addAttribute("projects",getProjects((String)req.getSession().getAttribute("email")));
			return "landingPage";
		} else if (!email.isEmpty()) {
			model.addAttribute("projectToSend", new Project());
			req.getSession().setAttribute("email", email);
			model.addAttribute("projects",getProjects((String)req.getSession().getAttribute("email")));
			model.addAttribute("email",email);
			return "landingPage";
		}
		model.addAttribute("user", new User());
		return "welcome";
	}

	@RequestMapping("/newproject")
	public String newProject(Model model,HttpServletRequest req, HttpServletResponse resp) {
		model.addAttribute("email", req.getSession().getAttribute("email"));
		model.addAttribute("project", new Project());
		return "newproject";
	}
	
	@RequestMapping("/Test")
	public String test(Model model,HttpServletRequest req, HttpServletResponse resp) {
		Project p = new Project();
		p.setUrl("https://github.com/cuuzis/java-project-for-sonar-scanner-testing.git");
		p.setName("Test");
		CommitAnalysis ca = new CommitAnalysis();
		ca.setConfigurationFile("my.properties");
		commitAnalysisDao.insert(ca);
		User usr = new User();
		usr.setEmail1("");
		usr.setPwd("");
		so.setAnalysis(ca);
		so.setProject(p);
		so.start();
		return "landingPage";
	}
	
	@RequestMapping("/landingPage")
	public String landingPaget(Model model, HttpServletRequest req, HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") == null)
			return "welcome";
		else {
			model.addAttribute("email", req.getSession().getAttribute("email"));
			model.addAttribute("projectToSend", new Project());
			model.addAttribute("projects",projectDao.findByemail("luca9294@hotmail.it"));			
			return "landingPage";
		}
	}

	@RequestMapping("/logout")
	public String logout(Model model, HttpServletRequest req, HttpServletResponse resp) {
		req.getSession().removeAttribute(""
				+ "email");
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
				model.addAttribute("email", emailSt);
		
			    model.addAttribute("projects",projectDao.findByemail(emailSt));
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
		for (Project p : projects){
			String url = p.getUrl();
			FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
	

		
			try {
				File d = new File("directory");
				Git git = Git.cloneRepository()
						.setURI(url)
						.setDirectory(d)
						.call();
				System.out.println(url);
				Iterable<RevCommit> commits = git.log().call();
				int count = 0;
				for (RevCommit commit : commits)
					count++;
				
				p.setTotalCommits(count);
				
				FileUtils.deleteDirectory(d);
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
		}
		
		
		return projects;
	}
	
	    
}
