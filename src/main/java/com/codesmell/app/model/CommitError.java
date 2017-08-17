package com.codesmell.app.model;

import java.util.Date;

public class CommitError extends BaseEntity{

	private String email;
	private String shaCommit;
	private String errorMessage;
	private Date errorDate;
	private String projectName;
	private int analysisId;
	
	public CommitError(String idCommit,String errorMessage)
	{
		this.shaCommit=idCommit;
		this.errorMessage=errorMessage;
		errorDate= new Date();
	}
	
	public CommitError() {
		this.errorMessage="";
	}

	public String getIdCommit() {
		return shaCommit;
	}

	public void setIdCommit(String shaCommit) {
		this.shaCommit = shaCommit;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public int getAnalysisId() {
		return analysisId;
	}

	public void setAnalysisId(int analysisId) {
		this.analysisId = analysisId;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public Date getDate() {
		return this.errorDate;
	}
	
	
}
