package com.codesmell.app.sonar;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.model.*;

import com.kotlin.*;

@Component
public class SonarAnalysis {
	private Project project  ;
	private CommitAnalysis analysis;
	
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private CommitDao commitDao;

	public SonarAnalysis() {
	}
	
	public void setProject(Project project){
		this.project = project;
	}
	
	public void setAnalysis(CommitAnalysis analysis){
		this.analysis = analysis;
	}
	
	public void runAnalysis(){
		//Analysis status is updated
		analysis.setStatus("Processing");
		analysis.setStartDate(new Date());
		commitAnalysisDao.save(analysis);
		
		String url  = project.getUrl();
		String conf = analysis.getConfigurationFile();
		String args[] = {"--git",url,"--properties",conf};
		ScanOptions so = ScanOptionsKt.parseOptions(args);
		
		File theDir = new File(project.getName() + "_" + analysis.getIdAnalysis());
		try {
			FileUtils.deleteDirectory(theDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Folder does not exists");
		}
        List<String> str = new LinkedList<String>();
		Git git = AppKt.cloneRemoteRepository(args[1], theDir);
		str = AppKt.analyseAllRevisions(git, so);
		
		//Each commits must be inserted in the db
		for (String commitStr : str){
			String[] commitArray = commitStr.split(" ");
			Commit commit = new Commit();
			commit.setAnalysisDate(new Date());
			commit.setSsa(commitArray[3]);
			commit.setIdCommitAnalysis(analysis.getIdAnalysis());
			commit.setStatus(commitArray[13]);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			try {
				commit.setCreationDate(df.parse(commitArray[2].replace("T", " ")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			commitDao.insert(commit);
		}
		
		analysis.setStatus("Finished");
		analysis.setEndDate(new Date());
		commitAnalysisDao.save(analysis);
	}
	
	

}
