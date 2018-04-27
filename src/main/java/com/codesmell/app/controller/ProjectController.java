package com.codesmell.app.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
import com.codesmell.app.model.Schedule;
import com.codesmell.app.sonar.SonarAnalysis;
import com.codesmell.app.sonar.SonarAnalysisManual;
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
		
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
		
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		project.setEmail(emailSt);
		//Project name must be unique
		if (projectDao.findByprojectName(project.getProjectName()) == null) {
			projectDao.save(project);
			writeConfigFile(project);
			cu.restNewProject(project.getName(), project.getUrl());
		}
		if (project.getScheduleProject()){
			projectDao.save(project);
			schedule(project, schedule);
			}
		else
			scheduleWithInterval(project, project.getInterval());

		cu.configureModelLandingPage(model, emailSt);
		return "landingPage";
	}
	
	
	/**
	 * It deletes a schedule from Quartz and from the db.
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
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
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
		scheduleDao.deleteByProjectName(project.getProjectName());
		cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
		return "landingPage";
	}
	
	
	@PostMapping("/modifySchedule")
	public String modifySchedule(Model model, @ModelAttribute Project project, HttpServletRequest req, HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.deleteJob(new JobKey(project.getProjectName(), project.getProjectName()));
			scheduleDao.deleteByProjectName(project.getProjectName());
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    project = projectDao.findByprojectName(project.getProjectName());
		cu.getUpdateProject(project);
		scheduleDao.deleteByProjectName(project.getProjectName());
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
			//scheduler.getContext().put("project", p);
			//scheduler.getContext().put("analysis", ca);
			job.getJobDataMap().put("project", p);
			job.getJobDataMap().put("interval", 1);
			//scheduler.getContext().put("interval", interval);
			scheduler.scheduleJob(job, runOnceTrigger);
			scheduler.start();
			Schedule schedule = new Schedule();
			schedule.setProjectName(p.getProjectName());
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
			schedule.setStartingDate(sdf.format(startDate));
			schedule.setRepetitionDay(0);
			schedule.setRepetitionHours(0);
			schedule.setRepetitionMinutes(totalMinutes);
			scheduleDao.save(schedule);
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
			job.getJobDataMap().put("project", p);
			int totalMinutes = s.getRepetitionDay() * 24 * 60 + s.getRepetitionHours() * 60 + s.getRepetitionMinutes();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
			Date startDate = new Date();
		    startDate = sdf.parse(s.getStartingDate());   
			
			Trigger runOnceTrigger = TriggerBuilder.newTrigger().startAt(startDate)
					.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(totalMinutes))
					.build();
			scheduler.getContext().put("commitAnalysisDao", commitAnalysisDao);
			scheduler.getContext().put("commitErrorDao", commitErrorDao);
			scheduler.getContext().put("commitDao", commitDao);
			scheduler.getContext().put("projectDao", projectDao);
			//scheduler.getContext().put("project", p);
			//scheduler.getContext().put("interval", 1);
			scheduler.scheduleJob(job, runOnceTrigger);
			scheduler.start();
			s.setRepetitionDay(0);
			s.setRepetitionHours(0);
			s.setRepetitionMinutes(totalMinutes);
			s.setProjectName(p.getProjectName());
			scheduleDao.save(s);
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
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		String emailSt = (String) req.getSession().getAttribute("email");
		model.addAttribute("email", emailSt);
		project.setEmail(emailSt);
		String projectName = project.getProjectName();
		if (project.getScheduleProject()){
		    project = projectDao.findByprojectName(project.getProjectName());
			schedule(project, schedule);}
		else{
		    project = projectDao.findByprojectName(project.getProjectName());
			scheduleWithInterval(project, project.getInterval());}
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
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
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
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
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
			writer.println("sonar.host.url=" + project.getSonarHost());
			writer.println("# Comma-separated paths to directories with sources (required)");
			writer.println("sonar.sources="+project.getSource());
			writer.println("# Encoding of the source files");
			writer.println("sonar.sourceEncoding=UTF-8");
			writer.println("gitRepo=" + project.getUrl());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * It removes a project and everything linked with it
	 * @param model
	 * @param projectToSend
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/removeProject")
	public String removeProject(Model model, @ModelAttribute Project projectToSend, HttpServletRequest req,
			HttpServletResponse resp) 
	{
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		projectToSend = this.projectDao.findByprojectName(projectToSend.getProjectName());
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
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
		
		project = this.projectDao.findByprojectName(project.getProjectName());
		model.addAttribute("email", (String) req.getSession().getAttribute("email"));
		model.addAttribute("project", project);

		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
				commitErrorDao);
		cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
		model.addAttribute("id", "");

		this.projectDao.save(project);


		return "editProject";
	}
	
	// to update the project in the ediProject.html
		@PostMapping("/updateProject")
		public String updateProject(Model model, @ModelAttribute Project project,
				HttpServletRequest req, HttpServletResponse resp) {
			if (req.getSession().getAttribute("email") == null) {
				return "welcome";
				}
			
			ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
					commitErrorDao);
		
			String emailSt = (String) req.getSession().getAttribute("email");
			model.addAttribute("email", emailSt);
			project.setEmail(emailSt);
					
				Project projectToBeSaved = projectDao.findBy_id(project.getTempProjectId());
				String oldProjectName = projectToBeSaved.getProjectName();
				
				projectToBeSaved.setProjectName(project.getProjectName());
				projectToBeSaved.setVersionType(project.getVersionType());
				projectToBeSaved.setUrl(project.getUrl());
				projectToBeSaved.setSonarKey(project.getSonarKey());
				projectToBeSaved.setSonarVersion(project.getSonarVersion());
				projectToBeSaved.setAnalysePast(project.getAnalysePast());
				projectToBeSaved.setSource(project.getSource());
				
				List<Commit> oldCommits = this.commitDao.findByProjectNameOrderByCreationDateDesc(oldProjectName);
				for(Commit c : oldCommits){
					c.setProjectName(project.getProjectName());
					this.commitDao.save(c);
				}
				
				List<CommitAnalysis> oldCommitsAnalysis = this.commitAnalysisDao.findByIdProject(oldProjectName);
				for(CommitAnalysis c : oldCommitsAnalysis){
					c.setIdProject(project.getProjectName());
					this.commitAnalysisDao.save(c);
				}
				
				Schedule oldSchedule = this.scheduleDao.findByProjectName(oldProjectName);
				if(oldSchedule != null){
					oldSchedule.setProjectName(project.getProjectName());
				}				
				
				
				projectDao.save(projectToBeSaved);
				new File(projectToBeSaved.getProjectName() + ".properties").deleteOnExit();
		
				writeConfigFile(projectToBeSaved);
				cu.modifyConfFile(project.getProjectName());
				
				cu.configureModelLandingPage(model, emailSt);
				return "landingPage";	
		}
	
   @PostMapping("/stopAnalysis")
   public String stopAnalysis(Model model, @ModelAttribute Project project, HttpServletRequest req,
			HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
	   CommitAnalysis ca  = commitAnalysisDao.findByIdProjectOrderByStartDateDesc(project.getProjectName());
	   commitAnalysisDao.delete(ca);
	   ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
		        commitErrorDao);
	   cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
	   return "landingPage";
   } 	
	
	/**
	 * It removes a project and everything linked with it
	 * @param model
	 * @param projectToSend
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/manualCommitInseration")
	public String manualCommitInseration(Model model, @ModelAttribute Project projectToSend, HttpServletRequest req,
			HttpServletResponse resp) 
	{
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		model.addAttribute("project",projectToSend);
		model.addAttribute("project2",new Project());

		model.addAttribute("email",(String) req.getSession().getAttribute("email"));
		return "manualCommitInsertion";
	}
	

	@PostMapping("/runManualCommitAnalysis")
	public String runManualCommitAnalysis(Model model, @ModelAttribute Project project2, HttpServletRequest req,
			HttpServletResponse resp) 
	{
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		String[] commitToAnalyze= project2.getManualCommitSSH().split("\\r?\\n");
		Project project =  this.projectDao.findByprojectName(project2.getProjectName());
		CommitAnalysis ca = new CommitAnalysis();
		ca.setIdProject(project.getProjectName());
		ca.setConfigurationFile(project.getProjectName() + ".properties");
		ca.setIdSerial(commitAnalysisDao.findByIdProject(project.getProjectName()).size() + 1);
		commitAnalysisDao.save(ca);
		SonarAnalysisManual so = new SonarAnalysisManual(commitAnalysisDao, commitDao, commitErrorDao);
		so.setAnalysis(ca);
		so.setProject(project);
		List<String> list = new LinkedList<String>();
		Collections.addAll(list, commitToAnalyze);
		so.setShas(list);
		so.start();
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,
		        commitErrorDao);
		cu.configureModelLandingPage(model, (String) req.getSession().getAttribute("email"));
		return "landingPage";
	}
	
	@PostMapping("/runScheduledNow")
 public String runScheduledNow(Model model, @ModelAttribute Project projectToSend, @ModelAttribute Schedule schedule,
                               HttpServletRequest req, HttpServletResponse resp) {
		
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
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
	
}
