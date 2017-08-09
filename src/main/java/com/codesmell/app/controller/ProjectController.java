package com.codesmell.app.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.Schedule;
import com.codesmell.app.sonar.SonarAnalysis;
import com.codesmell.app.sonar.SonarAnalysis2;

@EnableScheduling
@EnableAsync
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
	public String createNewProject(Model model, @ModelAttribute Project project,@ModelAttribute Schedule schedule, HttpServletRequest req,
			HttpServletResponse resp) {
		System.out.println(schedule.getRepetitionDay());
		System.out.println(schedule.getRepetitionHours());
		System.out.println(schedule.getRepetitionMinutes());
		System.out.println(schedule.getStartingDate());
		System.out.println(schedule.getStartingTime());
		
		
	
	       
	      
		
		
		
		
		
		
		
		/*String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		project.setEmail(emailSt);
		if (projectDao.findByurl(project.getUrl()).length == 0)
			if (projectDao.findByprojectName(project.getProjectName()).length == 0) {
				 projectDao.save(project);
				 writeConfigFile(project);
				if (project.getAnalysePast()){
					String projectName = (project.getProjectName());
					Project p = projectDao.findByprojectName(projectName)[0];
					CommitAnalysis ca = new CommitAnalysis();
					ca.setIdProject(p.getProjectName());
					ca.setConfigurationFile(projectName+".properties");
					commitAnalysisDao.insert(ca);
					SonarAnalysis so = new SonarAnalysis(commitAnalysisDao,commitDao);	
					so.setAnalysis(ca);
					so.setInterval(p.getInterval());
					so.setProject(p);
					so.start();
				
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		model.addAttribute("projects", getProjects(emailSt));*/
		return "landingPage";
	}
	
	private void schedule(){
		 try {
	        	
	       	 Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

		        JobDetail job = JobBuilder.newJob(SonarAnalysis2.class)
		            .withIdentity("2", "0") 
		            .build();

		        String startDateStr = "2017-08-08 21:24:00.0";
		        String endDateStr = "2013-09-31 00:00:00.0";

		        Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(startDateStr);
		       
		        Trigger runOnceTrigger = TriggerBuilder.newTrigger().startAt(startDate).build();
		        
		        Project p = projectDao.findByprojectName("SonarScanner")[0];
		        
		        CommitAnalysis ca = new CommitAnalysis();
				ca.setIdProject(p.getProjectName());
				ca.setConfigurationFile("SonarScanner"+".properties");
				commitAnalysisDao.insert(ca);
		        
		        
		     
		        	scheduler.getContext().put("commitAnalysisDao", commitAnalysisDao);
		        	scheduler.getContext().put("commitDao", commitDao);
		        	scheduler.getContext().put("project", p);
		        	scheduler.getContext().put("analysis", ca);
		        scheduler.getContext().put("interval", 5);
		        	
				scheduler.scheduleJob(job, runOnceTrigger);
				scheduler.start();
			} catch (SchedulerException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@PostMapping("/runAnalysis")
	public String newProject(Model model, @ModelAttribute Project projectToSend,HttpServletRequest req, HttpServletResponse resp) {
		if (projectToSend != null){
			if (commitAnalysisDao.findByIdProjectAndStatus(projectToSend.getProjectName(),"Processing") == null){
		String projectName = projectToSend.getProjectName();	
		Project p = projectDao.findByprojectName(projectName)[0];
		CommitAnalysis ca = new CommitAnalysis();
		ca.setIdProject(p.getProjectName());
		ca.setConfigurationFile(projectName+".properties");
		commitAnalysisDao.insert(ca);
		SonarAnalysis so = new SonarAnalysis(commitAnalysisDao,commitDao);	
		so.setAnalysis(ca);
		so.setProject(p);
		so.start();
		}
		}
		configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
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
			projectDao.save(p);
		}
		return projects;
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
