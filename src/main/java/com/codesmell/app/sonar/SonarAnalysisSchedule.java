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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SonarAnalysisSchedule implements org.quartz.Job {
	private Project project;
	private Commit lastCommit;
	private int interval = 1;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private CommitDao commitDao;
	private CommitErrorDao commitErrorDao;

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
	    SchedulerContext context = null;

		try {
			context = arg0.getScheduler().getContext();
		} catch (SchedulerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.project  = (Project)context.get("project");
		this.interval  = (int)context.get("interval");
		this.commitAnalysisDao = (CommitAnalysisDao)context.get("commitAnalysisDao");
		this.commitDao = (CommitDao)context.get("commitDao");
		this.commitErrorDao = (CommitErrorDao)context.get("commitErrorDao");
		if (commitDao.findByProjectNameOrderByCreationDateDesc(project.getProjectName()).get(0) != null)
			this.lastCommit = commitDao.findByProjectNameOrderByCreationDateDesc(project.getProjectName()).get(0);
		// Analysis status is updated
		CommitAnalysis ca = new CommitAnalysis();
		ca.setIdProject(project.getProjectName());
		ca.setConfigurationFile(project.getProjectName() + ".properties");
		commitAnalysisDao.insert(ca);
		ca.setIdSerial(commitAnalysisDao.findByIdProject(project.getProjectName()).size() + 1);
		ca.setStatus("Processing");
		ca.setStartDate(new Date());
		commitAnalysisDao.save(ca);

		String url = project.getUrl();
		String conf = ca.getConfigurationFile();
		String args[] = { "--git", url, "--properties", conf };
		
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

		File theDir = new File(project.getProjectName() + "_" + ca.getIdSerial());

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
			List<RevCommit> commitsList = new LinkedList<RevCommit>();
			for (RevCommit revCommit : commits) {
				if (revCommit.getCommitTime() > (this.lastCommit.getCreationDate().getTime()/1000))
					commitsList.add(0, revCommit);
			}
			
			for (RevCommit revCommit : commitsList) {
				if (count % interval == 0)
					flag = true;   
				else
					flag = false;
				count++;
				if (commitDao.findBySsa(revCommit.getName()) == null && flag) {
					String commitStr = new ControllerUtilities().restAnalysis(project.getProjectName(),revCommit.getName(),  ca.getIdSerial()+"",url);
					addCommit(commitStr,ca.getIdSerial());
				}
			}
			closeAnalysis(ca.get_id());
			FileUtils.deleteDirectory(theDir);
		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")));
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
	}
