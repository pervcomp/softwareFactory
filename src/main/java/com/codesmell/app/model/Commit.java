package com.codesmell.app.model;

import java.util.Date;

public class Commit extends BaseEntity{
	private String ssa;
	private String idCommitAnalysis;
	private Date creationDate;
	private Date analysisDate;
	private String status;
	private String projectName;
	
	public Commit() 
	{
		this.ssa="";
		this.creationDate=new Date();
		this.analysisDate=new Date();
		this.status="";
	}

	public String getSsa() {
		return ssa;
	}

	public void setSsa(String ssa) {
		this.ssa = ssa;
	}


	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
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
