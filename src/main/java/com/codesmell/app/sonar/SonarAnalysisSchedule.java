package com.codesmell.app.sonar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;

import code.codesmell.app.controllerUtilities.ControllerUtilities;
import code.codesmell.app.controllerUtilities.JSONHelper;


@Component
public class SonarAnalysisSchedule implements org.quartz.Job {
	private Project project;
	private Commit lastCommit;
	private CommitAnalysis analysis;
	private int interval = 1;
	private String urlWsVar = "http://sonar-scheduler-webservice:8090";
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private CommitDao commitDao;
	private CommitErrorDao commitErrorDao;
	private ProjectDao projectDao;

	public SonarAnalysisSchedule() {
	}

	public void setProject(Project project) {
		this.project = project;
	}

	
	public void setInterval (int interval){
		this.interval = interval;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		analysis = new CommitAnalysis();
		
		

		SchedulerContext context = null;
	   
		try {
			context = arg0.getScheduler().getContext();
		} catch (SchedulerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JobDataMap dataMap = arg0.getJobDetail().getJobDataMap();
		//fetch parameters from JobDataMap
			
		//this.project  = (Project)context.get("project");
		this.projectDao = (ProjectDao)context.get("projectDao");
		this.commitAnalysisDao = (CommitAnalysisDao)context.get("commitAnalysisDao");
		this.commitDao = (CommitDao)context.get("commitDao");
		
		this.project  = this.projectDao.findByprojectName(arg0.getTrigger().getJobKey().getName());
		this.interval = project.getInterval();

		this.commitErrorDao = (CommitErrorDao)context.get("commitErrorDao");
		
		// Analysis status is updated
		analysis.setIdProject(project.getProjectName());
		analysis.setConfigurationFile(project.getProjectName() + ".properties");
		commitAnalysisDao.insert(analysis);
		analysis.setIdSerial(commitAnalysisDao.findByIdProject(project.getProjectName()).size() + 1);
		analysis.setStartDate(new Date());
		commitAnalysisDao.save(analysis);
		long date = 0; 
        JSONHelper j = new JSONHelper(project);
        date = j.getLatestAnalysisDate();
		String url = project.getUrl();
		String conf = analysis.getConfigurationFile();
		
		String urlWs = urlWsVar + "/analyseRevision";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		String st = "";
		if (new File(project.getProjectName() + ".properties").exists()) {
			try {
				st = new String(Base64.encode(Files.readAllBytes(Paths.get(project.getProjectName() + ".properties"))), "UTF-8");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (commitAnalysisDao.findByIdProjectAndStatus(project.getProjectName(), "Processing").size() == 0){
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlWs).queryParam("projectName", project.getProjectName())
				.queryParam("analysis", analysis.getIdSerial()).queryParam("url", url).queryParam("date", date)
				.queryParam("conf", st);

		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		String temp = builder.build().encode().toUri().toString();
	    restTemplate.getForEntity(temp, String.class).getBody();
	    }
	
	}

	/**
	 *  Add commit to the db
	 * @param str
	 * @param analysisId
	 * @param app
	 */
	public void addCommit(String str, int analysisId) {
		String[] commitArray = str.split(" ");
		System.out.println(str);
			Commit commit = new Commit();
			commit.setAnalysisDate(new Date());
			commit.setProjectName(project.getProjectName());
			commit.setSsa(commitArray[3]);
			commit.setIdCommitAnalysis(analysisId);
			commit.setStatus(commitArray[13].replace(" ", "").replace(",", ""));
			String error = new ControllerUtilities().restGetActualError();
			
			//writing the commit error in the database
			writeCommitError(commit.getStatus(),commit.getSsa(),error,project.getProjectName(),analysisId);
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN);
			try {
				commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")).getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			commitDao.insert(commit);
		}
	

	/**
	 *  Set finish a processing analsis
	 * @param analysis    
	 */
	public void closeAnalysis(String analysis) {
		CommitAnalysis ca = commitAnalysisDao.findBy_id(analysis);
		ca.setStatus("Finished");
		ca.setEndDate(new Date());
		commitAnalysisDao.save(ca);

	}
	
	/**
	 * Writes the StackTrace on the db
	 * @param status
	 * @param idCommit
	 * @param message
	 */
	private void writeCommitError(String status,String idCommit,String message, String projectName, int analysisId)
	{
		if(status.equalsIgnoreCase("failure"))
		{
			CommitError commitError= new CommitError(idCommit, message);
			commitError.setAnalysisId(analysisId);
			commitError.setProjectName(projectName);
			commitError.setEmail(project.getEmail());
			commitErrorDao.insert(commitError);
		}
	}
	
	/**
	 * If there is an analysis Processing, the analysis is queued
	 */
	private void checkAvailability(){
		while(this.commitAnalysisDao.findByStatus("Processing").size() > 0){
			// Analysis status is updated
			analysis.setStatus("Queued");
			analysis.setStartDate(new Date());
			commitAnalysisDao.save(analysis);
		}
		// Analysis status is updated
			analysis.setStartDate(new Date());
			commitAnalysisDao.save(analysis);
	}

	}
