package com.codesmell.app.sonar;

import java.io.File;
import java.io.IOException;
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
import org.springframework.stereotype.Component;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.dao.ScheduleDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.Schedule;

import code.codesmell.app.controllerUtilities.ControllerUtilities;
import code.codesmell.app.controllerUtilities.JSONHelper;

@Component
public class SonarAnalysis extends Thread {
	private Project project;
	private CommitAnalysis analysis;
	private CommitAnalysisDao commitAnalysisDao;
	private CommitDao commitDao;
	private CommitErrorDao commitErrorDao;
	private ScheduleDao scheduleDao;
	private int interval = 1;
	private boolean justLatest = false;
	private boolean past = true;
	private Commit lastCommit;
	private Schedule scheduling;

	public SonarAnalysis(CommitAnalysisDao commitAnalysisDao, CommitDao commitDao, CommitErrorDao commitErrorDao, ScheduleDao scheduleDao) {
		this.commitAnalysisDao = commitAnalysisDao;
		this.commitDao = commitDao;
		this.commitErrorDao = commitErrorDao;
		this.scheduleDao = scheduleDao;
	}

	public Schedule getScheduling() {
		return scheduling;
	}

	public void setScheduling(Schedule scheduling) {
		this.scheduling = scheduling;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setAnalysis(CommitAnalysis analysis) {
		this.analysis = analysis;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setPast(boolean past) {
		this.past = past;
	}

	public void setJustLatest(boolean justLatest) {
		this.justLatest = justLatest;
	}

	public void setLastCommit(Commit lastCommit) {
		this.lastCommit = lastCommit;
	}

	@Override
	public void run() {
		// Analysis status is updated
		analysis.setStartDate(new Date());
		commitAnalysisDao.save(analysis);
		long startDate = 0; 
        JSONHelper j = new JSONHelper(project);
        startDate = j.getLatestAnalysisDate();
        this.scheduling = this.scheduleDao.findByProjectName(this.project.getProjectName());
        if (startDate < Long.parseLong(scheduling.getStartingDate()))
        		startDate = Long.parseLong(scheduling.getStartingDate());
		String url = project.getUrl();
		String conf = analysis.getConfigurationFile();
	    String r=  new ControllerUtilities().restAnalysis(project.getProjectName(),analysis.getIdSerial() + "", url,startDate, Long.parseLong(scheduling.getEndingDate()));

	}

	// Add commit to the db
	public void addCommit(String str, int analysisId) {
		String[] commitArray = str.split(" ");
		Commit commit = new Commit();
		commit.setAnalysisDate(new Date());
		commit.setProjectName(project.getProjectName());
		commit.setSsa(commitArray[3]);
		commit.setIdCommitAnalysis(analysisId);
		commit.setStatus(commitArray[13].replace(" ", "").replace(",", ""));
		String error = new ControllerUtilities().restGetActualError();

		// writing the commit error in the database
		writeCommitError(commit.getStatus(), commit.getSsa(), error, project.getProjectName(), analysisId);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN);
		try {
			commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")).getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (commitDao.findBySsa(commitArray[3]) == null)
			commitDao.save(commit);
	}

	/**
	 * It closes the opened analysis
	 * 
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
	 * 
	 * @param status
	 * @param idCommit
	 * @param message
	 */
	private void writeCommitError(String status, String idCommit, String message, String projectName, int analysisId) {
		if (status.equalsIgnoreCase("failure")) {
			CommitError commitError = new CommitError(idCommit, message);
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
	/*	while(this.commitAnalysisDao.findByStatus("Processing").size() > 0){
			// Analysis status is updated
			analysis.setStatus("Queued");
			analysis.setStartDate(new Date());
			commitAnalysisDao.save(analysis);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		// Analysis status is updated
			analysis.setStatus("Processing");
			analysis.setStartDate(new Date());
			commitAnalysisDao.save(analysis);
	}

}
