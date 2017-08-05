package com.codesmell.app.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.User;
import com.codesmell.app.sonar.SonarAnalysis;

@Controller
class ProjectController {

	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private CommitDao commitDao;
	@Autowired
	private UserDao userDao;

	@PostMapping("/createNewProject")
	public String createNewProject(Model model, @ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse resp) {
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		project.setEmail(emailSt);
		if (projectDao.findByurl(project.getUrl()).length == 0)
			if (projectDao.findByprojectName(project.getName()).length == 0) {
				projectDao.save(project);
				writeConfigFile(project);
			}
		model.addAttribute("projects", getProjects(emailSt));
		return "landingPage";
	}
	
	@PostMapping("/runAnalysis")
	public String newProject(Model model, @ModelAttribute Project projectToSend,HttpServletRequest req, HttpServletResponse resp) {
		String projectName = projectToSend.getProjectName();	
		String email = (String)req.getAttribute("email");
		Project p = projectDao.findByprojectName(projectName)[0];
		CommitAnalysis ca = new CommitAnalysis();
		ca.setConfigurationFile(projectName+".properties");
		commitAnalysisDao.insert(ca);
		User usr = userDao.findByEmail1(email);
		SonarAnalysis so = new SonarAnalysis(commitAnalysisDao,commitDao);	
		so.setAnalysis(ca);
		so.setProject(p);
		so.start();
		return "landingPage";
	}
	
	private void writeConfigFile(Project project) {
		try {
			File file = new File((project.getProjectName() + ".properties"));
			PrintWriter writer = new PrintWriter(file);
			writer.println("# Required metadata");
			writer.println("sonar.projectKey=" + project.getIdSonarKey());
			writer.println("sonar.projectName=" + project.getProjectName());
			writer.println("sonar.projectVersion=" + project.getSonarVersion());
			writer.println("sonar.host.url=http://sonar.inf.unibz.it/");
			writer.println("# Comma-separated paths to directories with sources (required)");
			writer.println("sonar.sources=.");
			writer.println("# Language");
			writer.println("sonar.language=java");
			writer.println("# Encoding of the source files");
			writer.println("sonar.sourceEncoding=UTF-8");
			writer.println("gitRepo=" + project.getUrl());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private List<Project> getProjects(String email) {
		List<Project> projects = projectDao.findByemail(email);
		for (Project p : projects) {
			String url = p.getUrl();
			FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();

			try {
				File d = new File("directory");
				Git git = Git.cloneRepository().setURI(url).setDirectory(d).call();
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
