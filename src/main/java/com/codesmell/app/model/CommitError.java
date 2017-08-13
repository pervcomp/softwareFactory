package com.codesmell.app.model;

public class CommitError extends BaseEntity{

	private String idCommit;
	private String errorMessage;
	
	public CommitError() {
		this.errorMessage="";
	}

	public String getIdCommit() {
		return idCommit;
	}

	public void setIdCommit(String idCommit) {
		this.idCommit = idCommit;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
}
