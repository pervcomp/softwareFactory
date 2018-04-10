package com.codesmell.app.sonar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;

import code.codesmell.app.controllerUtilities.ControllerUtilities;
import code.codesmell.app.controllerUtilities.JSONHelper;

@Component
public class SonarAnalysisManual extends Thread {
	private Project project;
	private CommitAnalysis analysis;
	private CommitAnalysisDao commitAnalysisDao;
	private CommitDao commitDao;
	private CommitErrorDao commitErrorDao;
	private List<String> shas;


	public SonarAnalysisManual(CommitAnalysisDao commitAnalysisDao, CommitDao commitDao, CommitErrorDao commitErrorDao) {
		this.commitAnalysisDao = commitAnalysisDao;
		this.commitDao = commitDao;
		this.commitErrorDao = commitErrorDao;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setAnalysis(CommitAnalysis analysis) {
		this.analysis = analysis;
	}
	
	public void setShas(List<String> shas) {
		this.shas = shas;
	}

	@Override
	public void run() {
		checkAvailability();
		long date = 0;
		this.checkAvailability();
        JSONHelper j = new JSONHelper(project);
        date = j.getLatestAnalysisDate();
		String url = project.getUrl();
		String conf = analysis.getConfigurationFile();
				new ControllerUtilities().restAnalysis(project.getProjectName(),
						analysis.getIdSerial() + "", url, date);
		closeAnalysis(analysis.get_id());

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
		while(this.commitAnalysisDao.findByStatus("Processing").size() > 0){
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
		}
		// Analysis status is updated
			analysis.setStatus("Processing");
			analysis.setStartDate(new Date());
			commitAnalysisDao.save(analysis);
	}

}
