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
import org.quartz.SimpleScheduleBuilder;
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
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.ScheduleDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.Schedule;
import com.codesmell.app.sonar.SonarAnalysis;
import com.codesmell.app.sonar.SonarAnalysis2;

import code.codesmell.app.controllerUtilities.ControllerUtilities;

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
	@Autowired
	private ScheduleDao scheduleDao;
	@Autowired
	private CommitErrorDao commitErrorDao;

	/**
	 * Response to createNewProject.
	 * It creates a new project. If pastAnalysis is selected, it launches an analysis of past commits
	 * @param model
	 * @param project
	 * @param schedule
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/createNewProject")
	public String createNewProject(Model model, @ModelAttribute Project project,@ModelAttribute Schedule schedule, HttpServletRequest req, HttpServletResponse resp) {		
	    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		project.setEmail(emailSt);
			if (projectDao.findByprojectName(project.getProjectName()) == null) {
				 projectDao.save(project);
				 writeConfigFile(project);
				if (project.getAnalysePast()){
					String projectName = (project.getProjectName());
					cu.performHistoryAnalysis(projectName);
				}
				String projectName = project.getProjectName();	
				cu.performAnalysisLatestsCommit(projectName);
			}
			if (project.getScheduleProject())
				schedule(project,schedule);
			
			
		cu.configureModelLandingPage(model, emailSt);
		
		
		return "landingPage";
	}
	
	private void schedule(Project p, Schedule s){
		 try {
	        	Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
	        	JobDetail job = JobBuilder.newJob(SonarAnalysis2.class)
		            .withIdentity(p.getProjectName(), "0") 
		            .build();
	        		int totalMinutes = s.getRepetitionDay()*24*60 + s.getRepetitionHours()*60 + s.getRepetitionMinutes();
		        Date startDate = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(s.getStartingDate() + " " + s.getStartingTime() );
		        Trigger runOnceTrigger = TriggerBuilder.newTrigger()
		        		.startAt(startDate)
		        		.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(totalMinutes)).build();
		        
		        CommitAnalysis ca = new CommitAnalysis();
				ca.setIdProject(p.getProjectName());
				ca.setConfigurationFile(p.getProjectName()+".properties");
				commitAnalysisDao.insert(ca);
		        
		        	scheduler.getContext().put("commitAnalysisDao", commitAnalysisDao);
		        	scheduler.getContext().put("commitDao", commitDao);
		        	scheduler.getContext().put("project", p);
		        	scheduler.getContext().put("analysis", ca);
		        scheduler.getContext().put("interval", 1);
		        	
				scheduler.scheduleJob(job, runOnceTrigger);
				scheduler.start();
				scheduleDao.save(s);
			} catch (SchedulerException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	/**
	 * It runs just the analysis of the latest commit. From Landing Page, "Run Analysis button"
	 * @param model
	 * @param projectToSend
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/runAnalysis")
	public String runJustLatestAnalysis(Model model, @ModelAttribute Project projectToSend,HttpServletRequest req, HttpServletResponse resp) {
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
		if (projectToSend != null){
			if (commitAnalysisDao.findByIdProjectAndStatus(projectToSend.getProjectName(),"Processing") == null){
				String projectName = projectToSend.getProjectName();	
				Project p = projectDao.findByprojectName(projectName);
				cu.performAnalysisLatestsCommit(projectName);
				}
			}
		cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
		return "landingPage";
	}
	
	private void writeConfigFile(Project project) {
		try {
			File file = new File((project.getProjectName() + ".properties"));
			PrintWriter writer = new PrintWriter(file);
			writer.println("# Required metadata");
			writer.println("sonar.projectKey=" + project.getSonarKey());
			writer.println("sonar.projectName=" + project.getProjectName());
			writer.println("sonar.projectVersion=" + project.getSonarVersion());
			writer.println("sonar.host.url=http://sonar.inf.ciao.it/");
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
	
}
