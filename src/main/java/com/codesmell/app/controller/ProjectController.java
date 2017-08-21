package com.codesmell.app.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
import org.quartz.JobKey;
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
import com.codesmell.app.dao.ProjectAnalysisDetailsDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.ScheduleDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.ProjetcAnalysisDetails;
import com.codesmell.app.model.Schedule;
import com.codesmell.app.model.User;
import com.codesmell.app.sonar.SonarAnalysis;
import com.codesmell.app.sonar.SonarAnalysisSchedule;
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
	@Autowired
	private ProjectAnalysisDetailsDao projectAnalysisDetailsDao;
	
	/**
	 * Response to createNewProject. It creates a new project. If pastAnalysis
	 * is selected, it launches an analysis of past commits
	 * 
	 * @param model
	 * @param project
	 * @param schedule
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/createNewProject")
	public String createNewProject(Model model, @ModelAttribute Project project, @ModelAttribute Schedule schedule,
			HttpServletRequest req, HttpServletResponse resp) {
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		project.setEmail(emailSt);
		if (projectDao.findByprojectName(project.getProjectName()) == null) {
			String port      = cu.getAvailablePortNumber();
			String container = cu.createContainerRest(port);
			project.setContainer(container);
			project.setPortNr(port);
			projectDao.save(project);
			writeConfigFile(project);
			if (project.getAnalysePast()) {
				String projectName = (project.getProjectName());
				cu.performHistoryAnalysis(projectName);
			}
			String projectName = project.getProjectName();
			cu.performAnalysisLatestsCommit(projectName);
		}
		if (project.getScheduleProject())
			schedule(project, schedule);
		else
			scheduleWithInterval(project, project.getInterval());

		cu.configureModelLandingPage(model, emailSt);
		return "landingPage";
	}
	
	
	/**
	 * It deletes a schedule.
	 * 
	 * @param model
	 * @param project
	 * @param schedule
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/deleteSchedule")
	public String deleteSchedule(Model model, @ModelAttribute Project project, HttpServletRequest req, HttpServletResponse resp) {
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.deleteJob(new JobKey(project.getProjectName(), project.getProjectName()));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
		return "landingPage";
	}
	
	
	@PostMapping("/modifySchedule")
	public String modifySchedule(Model model, @ModelAttribute Project project, HttpServletRequest req, HttpServletResponse resp) {
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.deleteJob(new JobKey(project.getProjectName(), project.getProjectName()));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    project = projectDao.findByprojectName(project.getProjectName());
		cu.getUpdateProject(project);
		cu.configureModelDetailsPage(model,(String)req.getSession().getAttribute("email"),project);
		return "projectDetails";
	}
	
	/**
	 * Analysis schedule each day at 12am and 12pm one commit each n new
	 * 
	 * @param p
	 * @param interval
	 */
	private void scheduleWithInterval(Project p, int interval) {
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			JobDetail job = JobBuilder.newJob(SonarAnalysisSchedule.class)
					.withIdentity(p.getProjectName(), p.getProjectName()).build();
			int totalMinutes = 6 * 60;
			Date startDate = new Date();
			if (startDate.getHours() < 12) {
				startDate.setHours(12);
				startDate.setMinutes(0);
				startDate.setSeconds(0);
			} else {
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, 1);
				startDate = c.getTime();
				startDate.setHours(0);
				startDate.setMinutes(0);
				startDate.setSeconds(0);

			}

			Trigger runOnceTrigger = TriggerBuilder.newTrigger().startAt(startDate)
					.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(totalMinutes)).build();
			CommitAnalysis ca = new CommitAnalysis();
			ca.setIdProject(p.getProjectName());
			ca.setConfigurationFile(p.getProjectName() + ".properties");
			commitAnalysisDao.insert(ca);
			ca.setIdSerial(commitAnalysisDao.findByIdProject(p.getProjectName()).size() + 1);
			scheduler.getContext().put("commitAnalysisDao", commitAnalysisDao);
			scheduler.getContext().put("commitDao", commitDao);
			scheduler.getContext().put("commitErrorDao", commitErrorDao);
			scheduler.getContext().put("project", p);
			scheduler.getContext().put("analysis", ca);
			scheduler.getContext().put("interval", interval);
			scheduler.scheduleJob(job, runOnceTrigger);
			scheduler.start();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * It schedule an analysis at the specific startDate and repetion.
	 * 
	 * @param p
	 * @param s
	 */
	private void schedule(Project p, Schedule s) {
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			JobDetail job = JobBuilder.newJob(SonarAnalysisSchedule.class)
					.withIdentity(p.getProjectName(), p.getProjectName()).build();
			int totalMinutes = s.getRepetitionDay() * 24 * 60 + s.getRepetitionHours() * 60 + s.getRepetitionMinutes();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
			Date startDate = new Date();
				if (s.getStartingTime() != null)
					startDate = sdf.parse(s.getStartingDate() + " " + s.getStartingTime());
				else
					startDate = sdf.parse(s.getStartingDate());
			
			    Trigger runOnceTrigger = TriggerBuilder.newTrigger().startAt(startDate)
					.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(totalMinutes))
					.build();
					
					CommitAnalysis ca = new CommitAnalysis();
			ca.setIdProject(p.getProjectName());
			ca.setConfigurationFile(p.getProjectName() + ".properties");
			commitAnalysisDao.insert(ca);
			ca.setIdSerial(commitAnalysisDao.findByIdProject(p.getProjectName()).size() + 1);
			scheduler.getContext().put("commitAnalysisDao", commitAnalysisDao);
			scheduler.getContext().put("commitErrorDao", commitErrorDao);
			scheduler.getContext().put("commitDao", commitDao);
			scheduler.getContext().put("project", p);
			scheduler.getContext().put("analysis", ca);
			scheduler.getContext().put("interval", 1);
			scheduler.scheduleJob(job, runOnceTrigger);
			scheduler.start();
		} catch (SchedulerException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * It reschedules an analysis. It is called from the project Details page
	 * 
	 * @param model
	 * @param projectToSend
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/reSchedule")
	public String reScheduler(Model model, @ModelAttribute Project project, @ModelAttribute Schedule schedule,
			HttpServletRequest req, HttpServletResponse resp) {
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		project.setEmail(emailSt);
		String projectName = project.getProjectName();
	    cu.performAnalysisLatestsCommit(projectName);
		if (project.getScheduleProject()){
		    project = projectDao.findByprojectName(project.getProjectName());
			schedule(project, schedule);}
		else{
		    project = projectDao.findByprojectName(project.getProjectName());
			scheduleWithInterval(project, project.getInterval());}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cu.getUpdateProject(project);
		cu.configureModelDetailsPage(model,(String)req.getSession().getAttribute("email"),project);
		return "projectDetails";
	}
	
	

	/**
	 * It runs just the analysis of the latest commit. From Landing Page, "Run
	 * Analysis button"
	 * 
	 * @param model
	 * @param projectToSend
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/runAnalysis")
	public String runJustLatestAnalysis(Model model, @ModelAttribute Project projectToSend, HttpServletRequest req,
			HttpServletResponse resp) {
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		if (projectToSend != null) {
			if (commitAnalysisDao.findByIdProjectAndStatus(projectToSend.getProjectName(), "Processing") == null) {
				String projectName = projectToSend.getProjectName();
				Project p = projectDao.findByprojectName(projectName);
				cu.performAnalysisLatestsCommit(projectName);
			}
		}
		cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
		return "landingPage";
	}
	
	@PostMapping("/stacktraceDetails")
	public String getStackTraceDetails(Model model, @ModelAttribute Commit commitDao, HttpServletRequest req,
			HttpServletResponse resp) {
		
		CommitError ce = this.commitErrorDao.findByShaCommit(commitDao.getSsa());
		model.addAttribute("commitError",ce);
		model.addAttribute("email",(String)req.getSession().getAttribute("email"));
		
		return "stacktraceDetails";
	}

	private void writeConfigFile(Project project) {
		try {
			File file = new File((project.getProjectName() + ".properties"));
			PrintWriter writer = new PrintWriter(file);
			writer.println("# Required metadata");
			writer.println("sonar.projectKey=" + project.getSonarKey());
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
	
	// to remove the project
	@PostMapping("/removeProject")
	public String removeProject(Model model, @ModelAttribute Project projectToSend, HttpServletRequest req,
			HttpServletResponse resp) 
	{
		removeProjectData(this.projectDao.findByprojectName(projectToSend.getProjectName()));
        ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
        commitErrorDao);
 		cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
		cu.deleteProjectFiles(projectToSend.getProjectName());
 		return "landingPage";
	}

	private void removeProjectData(Project project) 
	{
		//deleting the commit
		List<Commit> commitList= this.commitDao.findByProjectName(project.getProjectName());
		for(Commit commit: commitList)
			this.commitDao.delete(commit);
		
		//deleting the CommitAnalysis
		List<CommitAnalysis> commitAnalysisList= this.commitAnalysisDao.findByIdProject(project.getProjectName());
		for(CommitAnalysis commitAnalysis: commitAnalysisList)
			this.commitAnalysisDao.delete(commitAnalysis);
	   
		//remove the project
		this.projectDao.delete(project);
		
		Scheduler scheduler;
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.deleteJob(new JobKey(project.getProjectName(), project.getProjectName()));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// to edit the project
	@PostMapping("/editProject")
	public String editProject(Model model, @ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse resp) 
	{
		model.addAttribute("project", project);
		return "editProject";
	}
}
