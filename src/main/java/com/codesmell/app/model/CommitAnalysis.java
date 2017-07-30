package com.codesmell.app.model;

import java.util.Date;

public class CommitAnalysis extends BaseEntity{
	private String status;
	private Date startDate,endDate;
	private String idProject;
	private String configurationFile;
	
	public CommitAnalysis() {
		// TODO Auto-generated constructor stub
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getIdProject() {
		return idProject;
	}

	public void setIdProject(String idProject) {
		this.idProject = idProject;
	}
	
	public void setIdAnalysis(String id) {
		this._id = id;
	}
	
	public String getIdAnalysis() {
		return this._id ;
	}
	
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}
	
	public String getConfigurationFile() {
		return configurationFile ;
	}
	
	
	
}
