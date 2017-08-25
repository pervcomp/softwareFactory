package com.codesmell.app.sonar;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.Project;

import code.codesmell.app.controllerUtilities.ControllerUtilities;

@Component
public class SonarAnalysis extends Thread {
	private Project project;
	private CommitAnalysis analysis;
	private CommitAnalysisDao commitAnalysisDao;
	private CommitDao commitDao;
	private CommitErrorDao commitErrorDao;
	private int interval = 1;
	private boolean justLatest = false;

	public SonarAnalysis(CommitAnalysisDao commitAnalysisDao, CommitDao commitDao,CommitErrorDao commitErrorDao) {
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

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setJustLatest(boolean justLatest) {
		this.justLatest = justLatest;
	}

	@Override
	public void run() {
		// Analysis status is updated
		analysis.setStatus("Processing");
		analysis.setStartDate(new Date());
		commitAnalysisDao.save(analysis);

		String url = project.getUrl();
		String conf = analysis.getConfigurationFile();

		for (File f : new File(".").listFiles()) {
		    if (f.getName().startsWith(project.getProjectName() + "_") && f.isDirectory()) {
				try {
					FileUtils.deleteDirectory(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
		
		File theDir = new File(project.getProjectName() + "_" + analysis.getIdSerial());
		Git git = null;
		try {
			git = Git.cloneRepository()
					  .setURI(url)
					  .setDirectory(theDir).call();
		} catch (InvalidRemoteException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (TransportException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (GitAPIException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		int count = 0;
		boolean flag = false;
		Iterable<RevCommit> commits;
		try {
			commits = git.log().call();
			for (RevCommit revCommit : commits) {
				if (count % interval == 0)
					flag = true;
				else
					flag = false;
				count++;
				if (commitDao.findBySsa(revCommit.getName()) == null && flag) {
					String commitStr = new ControllerUtilities().restAnalysis(project.getProjectName(),revCommit.getName(),  analysis.getIdSerial()+"",url);
					addCommit(commitStr,analysis.getIdSerial());
				}
				if (justLatest)
					break;
			}
			closeAnalysis(analysis.get_id());
			FileUtils.deleteDirectory(theDir);
		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			
			//writing the commit error in the database
			writeCommitError(commit.getStatus(),commit.getSsa(),error,project.getProjectName(),analysisId);
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN);
			try {
				commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (commitDao.findBySsa(commitArray[3]) == null)
				commitDao.save(commit);
		}
	

	/**
	 * It closes the opened analysis
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
	

	
	
	
	
	

}
