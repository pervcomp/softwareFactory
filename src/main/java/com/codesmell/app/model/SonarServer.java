package com.codesmell.app.model;

import java.util.Date;

public class SonarServer {
	private String sonarServerUrl = "";
	private Date sonarDate = new Date();
	
	public SonarServer(){
		this.sonarServerUrl = "";
	}
	
	public String getSonarServerUrl() {
		return sonarServerUrl;
	}

	public void setSonarServerUrl(String sonarServerUrl) {
		this.sonarServerUrl = sonarServerUrl;
	}

	public Date getSonarDate() {
		return sonarDate;
	}

	public void setSonarDate(Date sonarDate) {
		this.sonarDate = sonarDate;
	}
	
	
	
}
