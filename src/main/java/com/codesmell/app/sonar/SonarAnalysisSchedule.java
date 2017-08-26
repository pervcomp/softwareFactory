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
		

		String[] splits = url.split("/");
		List<String>  shas = new LinkedList<String>();
		
		int i = 1;
		while (true) {
			try {
				String urlTemp = "https://api.github.com/repos/" + splits[3] + "/" + splits[4].replace(".git", "")
						+ "/commits?page=" + i + "&per_page=100";
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(urlTemp);
				httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("luca9294", "Aa30011992"),
						"UTF-8", false));
				HttpResponse httpResponse = httpClient.execute(httpGet);
				JSONArray json = new JSONArray(EntityUtils.toString(httpResponse.getEntity()));
				
				boolean found = false;
		
				
				if (found)
					break;
				
				for (int z = 0; z<json.length(); z++){
					JSONObject o = (JSONObject) json.get(z);
					if (!((String)o.get("sha")).equals(lastCommit.getSsa()))
						shas.add((String)o.get("sha"));
					else{
						found = true;
						break;
					}
					}
						
				if (found)
					break;
				
				if (json.length() == 0)
					break;
			i++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int count = 0;
		boolean flag = false;

			for (String sha : shas) {
				if (count % interval == 0)
					flag = true;   
				else
					flag = false;
				count++;
				if (commitDao.findBySsa(sha) == null && flag) {
					String commitStr = new ControllerUtilities().restAnalysis(project.getProjectName(),sha,  ca.getIdSerial()+"",url);
					addCommit(commitStr,ca.getIdSerial());
				}
			}
			closeAnalysis(ca.get_id());
	
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
