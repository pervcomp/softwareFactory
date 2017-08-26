package code.codesmell.app.controllerUtilities;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
import com.codesmell.app.sonar.SonarAnalysis;
import com.codesmell.app.sonar.SonarAnalysisSchedule;

public class ControllerUtilities {
	private ProjectDao projectDao;
	private CommitAnalysisDao commitAnalysisDao;
	private CommitDao commitDao;
	private UserDao userDao;
	private ScheduleDao scheduleDao;
	private CommitErrorDao commitErrorDao;
	private String urlWsVar = "http://34.211.54.69:8089";
	

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
	public ControllerUtilities(ProjectDao projectDao, CommitAnalysisDao commitAnalysisDao, CommitDao commitDao,
			UserDao userDao, ScheduleDao scheduleDao, CommitErrorDao commitErrorDao) {
		this.projectDao = projectDao;
		this.commitAnalysisDao = commitAnalysisDao;
		this.commitDao = commitDao;
		this.userDao = userDao;
		this.scheduleDao = scheduleDao;
		this.commitErrorDao = commitErrorDao;
	}

	public ControllerUtilities() {
	}

	/**
	 *      * It performs an analysis of all commits in the project till now.   
	 *   * interval = 1  will analyse all commits      * interval = n  will
	 * analyse a commit every 2      * @param interval     
	 */
	public void performHistoryAnalysis(String projectName) {
		Project project = projectDao.findByprojectName(projectName);
		CommitAnalysis ca = new CommitAnalysis();
		ca.setIdProject(projectName);
		ca.setConfigurationFile(projectName + ".properties");
		ca.setIdSerial(commitAnalysisDao.findByIdProject(projectName).size() + 1);
		commitAnalysisDao.save(ca);
		SonarAnalysis so = new SonarAnalysis(commitAnalysisDao, commitDao, commitErrorDao);
		so.setAnalysis(ca);
		so.setInterval(project.getInterval());
		so.setProject(project);
		so.start();
	}

	/**
	 * It analysis just the latest commit
	 * 
	 * @param projectName
	 */
	public void performAnalysisLatestsCommit(String projectName) {
		Project project = projectDao.findByprojectName(projectName);
		CommitAnalysis ca = new CommitAnalysis();
		ca.setIdProject(projectName);
		ca.setConfigurationFile(projectName + ".properties");
		ca.setIdSerial(commitAnalysisDao.findByIdProject(projectName).size() + 1);
		commitAnalysisDao.save(ca);
		SonarAnalysis so = new SonarAnalysis(commitAnalysisDao, commitDao, commitErrorDao);
		so.setAnalysis(ca);
		so.setInterval(project.getInterval());
		so.setProject(project);
		so.setJustLatest(true);
		so.start();
	}

	/**
	 * Sets mode for the landingPage
	 * 
	 * @param model
	 * @param email
	 */
	public void configureModelDetailsPage(Model model, String email, Project p) {
		CommitAnalysis analysis = commitAnalysisDao.findByIdProjectOrderByStartDateDesc(p.getProjectName());
		List<Commit> commits = commitDao.findByProjectNameOrderByCreationDateDesc(p.getProjectName());
		model.addAttribute("commits", commits);
		if (analysis != null)
			model.addAttribute("analysis", analysis);
		model.addAttribute("project", p);
		model.addAttribute("schedule", new Schedule());
		model.addAttribute("email", email);
		model.addAttribute("commitDao", new Commit());

	}

	/**
	 * Sets mode for the projectLandingPage
	 * 
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
	 * 
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
	 * Update variables of the project p (latest analysis, number of
	 * commits.....) @param project @throws
	 */
	public void getUpdateProject(Project project) {
		CommitAnalysis analysis = commitAnalysisDao.findByIdProjectOrderByStartDateDesc(project.getProjectName());
		String url = project.getUrl();
		if (project.getTotalCommits() == 0
				|| (((new Date().getTime() - project.getLastRequest().getTime()) / 1000 / 3600) > 2)) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					getCommitsCount(url, project);
				}
			});
			t.start();

		}
		if (analysis != null) {
			Date analysisDate = new Date();
			if (analysis.getStatus() == "Processing")
				analysisDate = analysis.getStartDate();
			else
				analysisDate = analysis.getEndDate();
			project.setLastAnalysis(analysisDate);
			project.setStatus(analysis.getStatus());
		} else {
			project.setLastAnalysis(null);
			project.setStatus("");
		}

		project.setAnalysedCommits(commitDao.findByProjectName(project.getProjectName()).size());
		project.setCountFailedCommits(
				(commitDao.findByProjectNameAndStatus(project.getProjectName(), "FAILURE").size()));
		project.setCountSuccessCommits(
				(commitDao.findByProjectNameAndStatus(project.getProjectName(), "SUCCESS").size()));
		if (getNextFire(project.getProjectName()) != null) {
			Date next = getNextFire(project.getProjectName());
			project.setNextAnalysis(next);
			Schedule schedule = scheduleDao.findByProjectName(project.getProjectName());
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
			schedule.setStartingDate(sdf.format(next));
			scheduleDao.save(schedule);
		} else {
			project.setNextAnalysis(null);
			Schedule schedule = scheduleDao.findByProjectName(project.getProjectName());
			if (schedule != null) {
				JobDetail job = JobBuilder.newJob(SonarAnalysisSchedule.class)
						.withIdentity(project.getProjectName(), project.getProjectName()).build();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
				sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
				Trigger runOnceTrigger = null;
				try {
					runOnceTrigger = TriggerBuilder.newTrigger().startAt(sdf.parse(schedule.getStartingDate()))
							.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(schedule.getRepetitionMinutes()))
							.build();
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				CommitAnalysis ca = new CommitAnalysis();
				ca.setIdProject(project.getProjectName());
				ca.setConfigurationFile(project.getProjectName() + ".properties");
				commitAnalysisDao.insert(ca);
				ca.setIdSerial(commitAnalysisDao.findByIdProject(project.getProjectName()).size() + 1);
			
				try {
					Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
					scheduler.getContext().put("commitAnalysisDao", commitAnalysisDao);
					scheduler.getContext().put("commitDao", commitDao);
					scheduler.getContext().put("commitErrorDao", commitErrorDao);
					scheduler.getContext().put("project", project);
					scheduler.getContext().put("analysis", ca);
					scheduler.getContext().put("interval", project.getInterval());
					scheduler.scheduleJob(job, runOnceTrigger);
					scheduler.start();
					Date next = getNextFire(project.getProjectName());
					project.setNextAnalysis(next);
					scheduleDao.save(schedule);
					
				} catch (SchedulerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		getReportColor(project);
		projectDao.save(project);
	}

	/**
	 * It gets total amount of commits of a git url
	 * 
	 * @param url
	 * @return
	 */
	private int getCommitsCount(String url, Project project) {
		int count = 0;
		try {
			FileUtils.deleteDirectory(new File("directory"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		File d = new File("directory");
		d.deleteOnExit();
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
		project.setTotalCommits(count);
		project.setLastRequest(new Date());
		this.projectDao.save(project);
		return count;
	}

	/**
	 * Next schedule from Quartz scheduler
	 * @param projectName
	 * @return
	 */
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
		;

		return date;
	}

	/**
	 * Color to display, it calculates percentage of failures commits of last
	 * week
	 * 
	 * @param project
	 */
	private void getReportColor(Project project) {
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DATE, -7);
		d.setTime(c.getTime().getTime());
		int failureLastWeek = commitDao
				.findByAnalysisDateGreaterThanAndProjectNameAndStatus(d, project.getProjectName(), "FAILURE").size();
		int totalCommitsLastWeek = commitDao.findByAnalysisDateGreaterThanAndProjectName(d, project.getProjectName())
				.size();
		if (totalCommitsLastWeek == 0)
			project.setLastWeekReport("GREEN");
		else {
			int percentage = (failureLastWeek / totalCommitsLastWeek * 100);
			if (percentage >= 70)
				project.setLastWeekReport("RED");
			else if (percentage >= 30)
				project.setLastWeekReport("YELLOW");
			else
				project.setLastWeekReport("GREEN");
		}
	}

	/**
	 * Schedules email with error to be sent at midnight
	 * @param usr
	 * @param mailSender
	 */
	public void scheduleDailyReport(User usr, JavaMailSender mailSender) {
		if (getNextFire(usr.getEmail1()) != null)
			return;
		else {
			try {
				Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
				JobDetail job = JobBuilder.newJob(MailUtilities.class).withIdentity(usr.getEmail1(), usr.getEmail1())
						.build();
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, 1);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);

				Trigger runOnceTrigger = TriggerBuilder.newTrigger().startAt(c.getTime())
						.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(24 * 60)).build();
				scheduler.getContext().put("mailSender", mailSender);
				scheduler.getContext().put("user", usr);
				scheduler.getContext().put("commitErrorDao", commitErrorDao);
				scheduler.scheduleJob(job, runOnceTrigger);
				scheduler.start();
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Run an analysis invoking a REST WEB SERVICE. Port parameter for Microservice version
	 * 
	 * @param projectName
	 * @param sha
	 * @param analysisId
	 * @param url
	 * @return
	 */
	public String restAnalysis(String projectName, String sha, String analysisId, String url) {
		String urlWs = urlWsVar + "/analyseRevision";
		/*if (port != null) {
		    urlWs = urlWsVar + ":" + port + "/analyseRevision";
			while (!pingHost(Integer.parseInt(port))) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}*/
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		String st = "";
		if (new File(projectName + ".properties").exists()) {
			try {
				st = new String(Base64.encode(Files.readAllBytes(Paths.get(projectName + ".properties"))), "UTF-8");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlWs).queryParam("projectName", projectName)
				.queryParam("sha", sha).queryParam("analysis", analysisId).queryParam("url", url)
				.queryParam("conf", st);

		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
		return restTemplate.getForEntity(temp, String.class).getBody();
	}

	/**
	 * It checks wheter the webservice for the analysis is available
	 * @param port
	 * @return
	 */
	private boolean pingHost() {

		String address = urlWsVar;
		try {
			final URL url = new URL(address);
			final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setConnectTimeout(1000 * 1); // mTimeout is in seconds
			final long startTime = System.currentTimeMillis();
			urlConn.connect();
			final long endTime = System.currentTimeMillis();
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				System.out.println("Time (ms) : " + (endTime - startTime));
				System.out.println("Ping to " + address + " was success");
				return true;
			}
		} catch (final MalformedURLException e1) {
			return false;
		} catch (final IOException e) {
			return false;
		}
		return false;
	}

	/**
	 * It gets Actual Error using REST web Service
	 * 
	 * @return
	 */
	public String restGetActualError() {
		String urlWs = urlWsVar + "/getActualError";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlWs);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
		return restTemplate.getForEntity(temp, String.class).getBody();
	}

	/**
	 * Deletes temp file of a deleted project
	 * 
	 * @return
	 */
	public void deleteProjectFiles(String projectName) {
		String urlWs = urlWsVar + "/deleteProject";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlWs).queryParam("projectName", projectName);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
	}

	/**
	 * NOT USED IN THE CURRENT VERSION
	 * It creates a new Docker Container at the specified port
	 * for the microservice version
	 * @param portNr
	 * @return
	 */
	public String createContainerRest(String portNr) {
		String urlWs = urlWsVar + ":8080/createMicroservice/";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlWs).queryParam("port", portNr);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
		String result = restTemplate.getForEntity(temp, String.class).getBody();
		return result.substring(0, 12);
	}

	/**
	 * NOT USED IN THE CURRENT VERSION
	 * It creates a new Docker Container at the specified port
	 * 
	 * @param portNr
	 * @return
	 */
	public String deleteContainerRest(String container) {
		String urlWs = urlWsVar + ":8080/deleteMicroservice/";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlWs).queryParam("container", container);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
		return restTemplate.getForEntity(temp, String.class).getBody();
	}

	/**
	 * Used for the microservice version
	 * @return
	 */
	public String getAvailablePortNumber() {
		Random r = new Random();
		int low = 8000;
		int high = 9000;
		int result = r.nextInt(high - low) + low;
		while (projectDao.findByPortNr(result + "") != null)
			result = r.nextInt(high - low) + low;
		return result + "";
	}

}
