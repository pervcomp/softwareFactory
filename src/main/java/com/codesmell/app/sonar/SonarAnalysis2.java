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
import org.eclipse.jgit.revwalk.RevCommit;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.kotlin.App;
import com.kotlin.ScanOptionsKt;

@Component
public class SonarAnalysis2 implements org.quartz.Job {
	private Project project;
	private App app;
	private Date startDate;
	private Commit lastCommit;
	private CommitAnalysis analysis;
	private int interval = 1;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private CommitDao commitDao;

	public SonarAnalysis2() {
     app = new App();
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setAnalysis(CommitAnalysis analysis) {
		this.analysis = analysis;
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
		this.analysis = (CommitAnalysis)context.get("analysis");
		this.project  = (Project)context.get("project");
		this.interval  = (int)context.get("interval");
		this.commitAnalysisDao = (CommitAnalysisDao)context.get("commitAnalysisDao");
		this.commitDao = (CommitDao)context.get("commitDao");
        this.lastCommit = commitDao.findByProjectNameOrderByCreationDateDesc(project.getProjectName()).get(0);

		
		// TODO Auto-generated method stub
		// Analysis status is updated
		analysis.setStatus("Processing");
		analysis.setStartDate(new Date());
		commitAnalysisDao.save(analysis);

		String url = project.getUrl();
		String conf = analysis.getConfigurationFile();
		String args[] = { "--git", url, "--properties", conf };
		com.kotlin.ScanOptions so = ScanOptionsKt.parseOptions(args);

		File theDir = new File(project.getName() + "_" + analysis.getIdAnalysis());
		try {
			FileUtils.deleteDirectory(theDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Folder does not exists");
		}
	    app = new App();
		Git git = app.cloneRemoteRepository(args[1], theDir);
		List<RevCommit> commitsList = new LinkedList<RevCommit>();

		try {
			int count = 0;
			boolean flag = false;
			Iterable<RevCommit> commits = git.log().call();
			for (RevCommit revCommit : commits) {
				if (revCommit.getCommitTime() > this.lastCommit.getCreationDate().getTime())
					commitsList.add(0, revCommit);
			}
			for (RevCommit revCommit : commits){
			if (commitDao.findBySsa(revCommit.getName()) == null && flag){
				String commitStr = app.analyseRevision(git, so, revCommit.getName());
				String[] commitArray = commitStr.split(" ");
				Commit commit = new Commit();
				commit.setAnalysisDate(new Date());
				commit.setSsa(commitArray[3]);
				commit.setIdCommitAnalysis(analysis.getIdSerial());
				commit.setStatus(commitArray[13]);
				commit.setProjectName(analysis.getIdProject());
				String mistake = app.getActualError();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
				try {
					commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				commitDao.insert(commit);
				}
			}
			

		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		analysis.setStatus("Finished");
		analysis.setEndDate(new Date());
		commitAnalysisDao.save(analysis);
		try {
			FileUtils.deleteDirectory(theDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Folder does not exists");
		}

	}

	public void addCommit(String str, String analysis, App app) {
		String[] strArray = str.split(" ");
		// Each commits must be inserted in the db
		for (String commitStr : strArray) {
			String[] commitArray = commitStr.split(" ");
			Commit commit = new Commit();
			commit.setAnalysisDate(new Date());
			commit.setSsa(commitArray[3]);
	       //	commit.setIdCommitAnalysis(analysis);
			commit.setStatus(commitArray[13]);
			String mistake = app.getActualError();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			try {
				commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			commitDao.insert(commit);
		}
	}

	public void closeAnalysis(String analysis) {
		CommitAnalysis ca = commitAnalysisDao.findBy_id(analysis);
		ca.setStatus("Finished");
		ca.setEndDate(new Date());
		commitAnalysisDao.save(ca);

	}	
	}
