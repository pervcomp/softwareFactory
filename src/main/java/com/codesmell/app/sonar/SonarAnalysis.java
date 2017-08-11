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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.kotlin.AppKt;
import com.kotlin.ScanOptionsKt;

@Component
public class SonarAnalysis extends Thread {
	private Project project;
	private CommitAnalysis analysis;
	private int interval = 1;
	private boolean justLatest = false;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private CommitDao commitDao;

	public SonarAnalysis(CommitAnalysisDao commitAnalysisDao, CommitDao commitDao) {
		this.commitAnalysisDao = commitAnalysisDao;
		this.commitDao = commitDao;
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
		String args[] = { "--git", url, "--properties", conf };
		com.kotlin.ScanOptions so = ScanOptionsKt.parseOptions(args);

		File theDir = new File(project.getProjectName() + "_" + analysis.getIdAnalysis());
		try {
			FileUtils.deleteDirectory(theDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Folder does not exists");
		}
		Git git = AppKt.cloneRemoteRepository(args[1], theDir);
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
					String commitStr = AppKt.analyseRevision(git, so, revCommit.getName());
					addCommit(commitStr,analysis.get_id());
					if (justLatest)
						break;
				}
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
	public void addCommit(String str, String analysisId) {
		String[] commitArray = str.split(" ");
		System.out.println(str);
			Commit commit = new Commit();
			commit.setAnalysisDate(new Date());
			commit.setProjectName(project.getProjectName());
			commit.setSsa(commitArray[3]);
			commit.setIdCommitAnalysis(analysisId);
			commit.setStatus(commitArray[13]);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			try {
				commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			commitDao.save(commit);
		}
	

	// Set finish a processing analsis
	public void closeAnalysis(String analysis) {
		CommitAnalysis ca = commitAnalysisDao.findBy_id(analysis);
		ca.setStatus("Finished");
		ca.setEndDate(new Date());
		commitAnalysisDao.save(ca);

	}

}
