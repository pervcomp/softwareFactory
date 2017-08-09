package com.codesmell.app.model;

import java.util.Date;

public class Commit extends BaseEntity{
	private String ssh;
	private String idCommitAnalysis;
	private Date commitDate;
	private Date analysisDate;
	private String status;
	private String projectName;
	
	public Commit() 
	{
		this.ssh="";
		this.commitDate=new Date();
		this.analysisDate=new Date();
		this.status="";
	}

	public String getSsa() {
		return ssh;
	}

	public void setSsa(String ssa) {
		this.ssh = ssa;
	}


	public Date getCreationDate() {
		return commitDate;
	}

	public void setCreationDate(Date creationDate) {
		this.commitDate = creationDate;
	}

	public Date getAnalysisDate() {
		return analysisDate;
	}

	public void setAnalysisDate(Date analysisDate) {
		this.analysisDate = analysisDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIdCommitAnalysis() {
		return idCommitAnalysis;
	}

	public void setIdCommitAnalysis(String idCommitAnalysis) {
		this.idCommitAnalysis = idCommitAnalysis;
	}
	
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
}
