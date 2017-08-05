package com.codesmell.app.model;

import java.util.Date;

public class Project extends BaseEntity
{
	private String projectName;
	private Date creationTime;
	private String url;
	private String versionType;
	private Date analysisStartDate;
	private String anaysisRepetition;
	private String idUser;
	private String email;
	private String sonarKey;
	private String sonarVersion;
	private int totalCommits;
	private int analysedCommits;
	
	public Project() 
	{
		this.creationTime= new Date();
		this.url="";
		this.versionType="git";
		this.analysisStartDate=new Date();
		this.anaysisRepetition="";
	}
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getName() {
		return url;
	}
	public void setName(String url) {
		this.url = url;
	}
	
	
	
	public String getVersionType() {
		return versionType;
	}
	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}
	public Date getAnalysisStartDate() {
		return analysisStartDate;
	}
	public void setAnalysisStartDate(Date analysisStartDate) {
		this.analysisStartDate = analysisStartDate;
	}
	public String getAnaysisRepetition() {
		return anaysisRepetition;
	}
	public void setAnaysisRepetition(String anaysisRepetition) {
		this.anaysisRepetition = anaysisRepetition;
	}

	public String getIdUser() {
		return idUser;
	}

	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getIdSonarKey() {
		return sonarKey;
	}

	public void setIdSonarKey(String sonarKey) {
		this.sonarKey = sonarKey;
	}
	public String getSonarVersion() {
		return sonarVersion;
	}

	public void setSonarVersion(String sonarVersion) {
		this.sonarVersion = sonarVersion;
	}
	public int getAnalysedCommits() {
		return this.analysedCommits;
	}

	public void setAnalysedCommits(int analysedCommits) {
		this.analysedCommits = analysedCommits;
	}
	
	public int getTotalCommits() {
		return  totalCommits;
	}

	public void setTotalCommits(int totalCommits) {
		this.totalCommits = totalCommits;
	}
	
}
