package com.codesmell.app.model;

import java.util.Date;

public class Project extends BaseEntity
{
	private String name;
	private Date creationTime;
	private String url;
	private String versionType;
	private Date analysisStartDate;
	private String anaysisRepetition;
	private String idUser;
	
	public Project() 
	{
		this.name="";
		this.creationTime= new Date();
		this.url="";
		this.versionType="git";
		this.analysisStartDate=new Date();
		this.anaysisRepetition="";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	
	
	
}
