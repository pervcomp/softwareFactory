package code.codesmell.app.controllerUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
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
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ControllerUtilities {
	private ProjectDao projectDao;
	private CommitAnalysisDao commitAnalysisDao;
	private CommitDao commitDao;
	private UserDao userDao;
	private ScheduleDao scheduleDao;
	private CommitErrorDao commitErrorDao;
	private String urlWsVar = "http://webservice1:8090";

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
		so.setInterval(project.getPastInterval());
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
		so.setPast(false);
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
		writeConfigFile( project);
		System.out.println(project.getProjectName());
		CommitAnalysis analysis = commitAnalysisDao.findByIdProjectOrderByStartDateDesc(project.getProjectName());
		String url = project.getUrl();

		CompletableFuture.runAsync(() -> {
				getCommitsCount(url, project);
			});
	
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
				job.getJobDataMap().put("project", project);
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
					job.getJobDataMap().put("project", project);
					job.getJobDataMap().put("interval", 1);
	
					scheduler.getContext().put("analysis", ca);
					//scheduler.getContext().put("interval", project.getInterval());
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
			writer.println("sonar.github.repository=" + project.getUrl());
			writer.println("sonar.jira.url="+project.getJiraUrl());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * It gets total amount of commits of a git url
	 * 
	 * @param url
	 * @return
	 */
	private int getCommitsCount(String url, Project project) {
		int count = 0;
	 	Git git = null;
		String projectName = project.getProjectName();
	 	if (!new File(projectName).exists()) {
        try {
        	git = Git.cloneRepository()
        			.setURI(url)
        			.setDirectory(new File(project.getProjectName()))
        			.setCloneAllBranches( true )
        			.call();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
    	}
	 	else{
	 		FileRepositoryBuilder builder = new FileRepositoryBuilder();
	 		Repository repo;
			try {
				repo = builder.setGitDir(new File(projectName+"/.git")).setMustExist(true).build();
				git = new Git(repo);
				git.pull();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 	
	 	}
		
			Iterable<RevCommit> commits;
			try {
				commits = git.log().call();
				long date = 0;
				for (RevCommit commit : commits){
					long temp = 0;
					if (count==0){
						temp = commit.getCommitTime();
						project.setLatestCommitDt(new Date(temp*1000));
						}
					date = (commit.getCommitTime());
					count++;
				}
			project.setCreationTime(new Date((date*1000)));
			project.setTotalCommits(count);
			project.setCountCommitsLeft(count-getNumberAnalysedCommits(project));
			project.setLastRequest(new Date());
			date = commitDao.findByProjectNameOrderByCreationDateDesc(projectName).get(0).getCreationDate();
			project.setLatestCommitAnalysedDt(new Date(date*1000));
			this.projectDao.save(project);
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	
	 	return count;}
	
	
	
	public int getNumberAnalysedCommits(Project p){
		String sURL = p.getSonarHost() + "/api/project_analyses/search?project=" + p.getSonarKey();
		HttpURLConnection request = null;
		URL url;
		try {
			url = new URL(sURL);
			request = (HttpURLConnection) url.openConnection();
		    request.connect();
		    
		    // Convert to a JSON object to print data
		    JsonParser jp = new JsonParser(); //from gson
		    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
		    JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object. 
		    JsonObject obj = rootobj.getAsJsonObject("paging");
		    return obj.get("total").getAsInt();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			 return 0;} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	
	}
	
	

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	
	private void execute(String command, File directory)
			throws Exception {
		System.out.println("$ " + command);
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.directory(directory);
		pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.PIPE);
        Process p = pb.start();
        
        BufferedReader reader = 
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
String result = builder.toString();
        int test = p.waitFor();
        while (p.isAlive()){
        System.out.println(p.waitFor());
        System.out.println(p.exitValue());
        Thread.sleep(10000);
        }
	}
	/**
	 * Next schedule from Quartz scheduler
	 * 
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
	 * 
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
	 * Run an analysis invoking a REST WEB SERVICE. Port parameter for
	 * Microservice version
	 * 
	 * @param projectName
	 * @param sha
	 * @param analysisId
	 * @param url
	 * @return
	 */
	public String restAnalysis(String projectName, String analysisId, String url, long date) {
		String urlWs = urlWsVar + "/analyseRevision";
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
				.queryParam("analysis", analysisId).queryParam("url", url).queryParam("date", date)
				.queryParam("conf", st);

		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
		return restTemplate.getForEntity(temp, String.class).getBody();
	}
	
	/**
	 * Run an analysis invoking a REST WEB SERVICE. Port parameter for
	 * Microservice version
	 * 
	 * @param projectName
	 * @param sha
	 * @param analysisId
	 * @param url
	 * @return
	 */
	public String restNewProject(String projectName, String url) {
		String urlWs = urlWsVar + "/newProject";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlWs).queryParam("projectName", projectName)
        .queryParam("url", url);

		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
		return restTemplate.getForEntity(temp, String.class).getBody();
	}

	/**
	 * It checks wheter the webservice for the analysis is available
	 * 
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
	 * Modify Conf Files
	 * 
	 * @return
	 */
	public String modifyConfFile(String projectName) {
		String urlWs = urlWsVar + "/updateConfFile";
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
				.queryParam("conf", st);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
		return restTemplate.getForEntity(temp, String.class).getBody();
	}
}
