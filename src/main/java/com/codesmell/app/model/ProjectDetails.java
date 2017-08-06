package com.codesmell.app.model;

import java.util.Date;
import java.util.List;

public class ProjectDetails extends BaseEntity
{
	private Project project;
	private Date lastAnalysis;
	private List<Commit> commitList;
	
	public ProjectDetails() {
		// TODO Auto-generated constructor stub
	}
	
	public ProjectDetails(Project project,Date lastAnalysis,List<Commit> commitList)
	{
		this.project= project;
		this.lastAnalysis= lastAnalysis;
		this.commitList= commitList;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Date getLastAnalysis() {
		return lastAnalysis;
	}

	public void setLastAnalysis(Date lastAnalysis) {
		this.lastAnalysis = lastAnalysis;
	}

	public List<Commit> getCommitList() {
		return commitList;
	}

	public void setCommitList(List<Commit> commitList) {
		this.commitList = commitList;
	}
	
	
}