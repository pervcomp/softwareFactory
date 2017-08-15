package code.codesmell.app.controllerUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.ScheduleDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.sonar.SonarAnalysis;

public class ControllerUtilities {
	private ProjectDao projectDao;
	private CommitAnalysisDao commitAnalysisDao;
	private CommitDao commitDao;
	private UserDao userDao;
	private ScheduleDao scheduleDao;
	private CommitErrorDao commitErrorDao;
	
	/**
	 * This class contains helpers methods that required from the controllers.
	 * 
	 * 
	 * @param projectDao
	 * @param commitAnalysisDao
	 * @param commitDao
	 * @param userDao
	 * @param scheduleDao
	 */
	public ControllerUtilities (ProjectDao projectDao, CommitAnalysisDao commitAnalysisDao,CommitDao commitDao,
			UserDao userDao, ScheduleDao scheduleDao,CommitErrorDao commitErrorDao	){
		this.projectDao = projectDao;
		this.commitAnalysisDao = commitAnalysisDao;
		this.commitDao = commitDao;
		this.userDao   = userDao; 
		this.scheduleDao = scheduleDao;
		this.commitErrorDao =  commitErrorDao;
	}
	
    /**
     * It performs an analysis of all commits in the project till now.
     * interval = 1  will analyse all commits
     * interval = n  will analyse a commit every 2
     * @param interval
     */
	public void performHistoryAnalysis(String projectName){
		Project project = projectDao.findByprojectName(projectName);
		CommitAnalysis ca = new CommitAnalysis();
		ca.setIdProject(projectName);
		ca.setConfigurationFile(projectName+".properties");
		ca.setIdSerial(commitAnalysisDao.findByIdProject(projectName).size()+1);
		commitAnalysisDao.save(ca);
		SonarAnalysis so = new SonarAnalysis(commitAnalysisDao,commitDao,commitErrorDao);	
		so.setAnalysis(ca);
		so.setInterval(project.getInterval());
		so.setProject(project);
		so.start();
	}
	
	
	/**
	 * It analysis just the latest commit
	 * @param projectName
	 */
	public void performAnalysisLatestsCommit(String projectName){
		Project project = projectDao.findByprojectName(projectName);
		CommitAnalysis ca = new CommitAnalysis();
		ca.setIdProject(projectName);
		ca.setConfigurationFile(projectName+".properties");
		ca.setIdSerial(commitAnalysisDao.findByIdProject(projectName).size()+1);
		commitAnalysisDao.save(ca);
		SonarAnalysis so = new SonarAnalysis(commitAnalysisDao,commitDao,commitErrorDao);	
		so.setAnalysis(ca);
		so.setInterval(project.getInterval());
		so.setProject(project);
		so.setJustLatest(true);
		so.start();
	}
	
	/**
	 * Sets mode for the landingPage
	 * @param model
	 * @param email
	 */
	public void configureModelLandingPage(Model model, String email) {
		model.addAttribute("projects", getProjects(email));
		model.addAttribute("email", email);
		model.addAttribute("projectToSend", new Project());
	}
	
	/**
	 * Updates all projects of a user
	 * @param email
	 * @return
	 */
	private List<Project> getProjects(String email) {
		List<Project> projects = projectDao.findByemail(email);
		for (Project p : projects) {
			getUpdateProject(p);
		}
		return projects;
	}
	
	/** 
	 * Update variables of the project p (latest analysis, number of commits.....)
	 * @param project
	 */
	public void getUpdateProject(Project project){
		CommitAnalysis analysis = commitAnalysisDao.findByIdProjectOrderByStartDateDesc(project.getProjectName());
		String url = project.getUrl();
		if (project.getTotalCommits() == 0 || (((new Date().getTime() - project.getLastRequest().getTime()) / 1000 / 3600) > 6)) {
			int count = getCommitsCount(url);
			project.setTotalCommits(count);
			project.setLastRequest(new Date());
		}
		if (analysis != null){
		Date analysisDate = new Date();
		if (analysis.getStatus() == "Processing")
			analysisDate = analysis.getStartDate();
		else
			analysisDate = analysis.getEndDate();
		project.setLastAnalysis(analysisDate);
		project.setStatus(analysis.getStatus());
		}
	    project.setAnalysedCommits(commitDao.findByProjectName(project.getProjectName()).size());
		project.setCountFailedCommits((commitDao.findByProjectNameAndStatus(project.getProjectName(), "FAILURE").size()));
		project.setCountSuccessCommits((commitDao.findByProjectNameAndStatus(project.getProjectName(), "SUCCESS").size()));
		if (getNextFire(project.getProjectName()) != null)
			project.setNextAnalysis(getNextFire(project.getProjectName()));
		projectDao.save(project);
	}
	
	/**
	 * It gets total amount of commits of a git url
	 * @param url
	 * @return
	 */
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
	
	private Date getNextFire(String projectName) {
		Date date = null;
		try {
			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			for (String groupName : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(projectName))) {
					String jobName = jobKey.getName();
					String jobGroup = jobKey.getGroup();
					List<Trigger> triggers;
					triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
					date = triggers.get(0).getNextFireTime();
				}
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return date;
	}
	
}
