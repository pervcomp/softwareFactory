package com.codesmell.app.model;

public class ProjetcAnalysisDetails extends BaseEntity
{
	private int noOfCommits;
	private int noOfAnalyzedCommits;
	private int noOfSucessfullyAnalyzedCommits;
	private int noOfFailedAnalyzedCommits;
	private String idProject;
	
	public ProjetcAnalysisDetails() 
	{
		this.noOfCommits=0;
		this.noOfAnalyzedCommits=0;
		this.noOfSucessfullyAnalyzedCommits=0;
		this.noOfFailedAnalyzedCommits=0;
		
	}
	
	public String getIdProject() {
		return idProject;
	}
	public void setIdProject(String idProjet) {
		this.idProject = idProjet;
	}
	public int getNoOfCommits() {
		return noOfCommits;
	}
	public void setNoOfCommits(int noOfCommits) {
		this.noOfCommits = noOfCommits;
	}
	public int getNoOfAnalyzedCommits() {
		return noOfAnalyzedCommits;
	}
	public void setNoOfAnalyzedCommits(int noOfAnalyzedCommits) {
		this.noOfAnalyzedCommits = noOfAnalyzedCommits;
	}
	public int getNoOfSucessfullyAnalyzedCommits() {
		return noOfSucessfullyAnalyzedCommits;
	}
	public void setNoOfSucessfullyAnalyzedCommits(int noOfSucessfullyAnalyzedCommits) {
		this.noOfSucessfullyAnalyzedCommits = noOfSucessfullyAnalyzedCommits;
	}
	public int getNoOfFailedAnalyzedCommits() {
		return noOfFailedAnalyzedCommits;
	}
	public void setNoOfFailedAnalyzedCommits(int noOfFailedAnalyzedCommits) {
		this.noOfFailedAnalyzedCommits = noOfFailedAnalyzedCommits;
	}

	
	
}
