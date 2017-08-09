package com.codesmell.app.model;

import java.util.Date;

public class Schedule extends BaseEntity{
	private int repetitionDay;
	private int repetitionHours;
	private int repetitionMinutes;
	private String startingDate;
	private String startingTime;
	private String userEmail;
	private String projectName;
	
	public Schedule() 
	{
		this.repetitionDay=0;
		this.userEmail="";
	}

	public int getRepetitionDay() {
		return repetitionDay;
	}

	public void setRepetitionDay(int repetitionDay) {
		this.repetitionDay = repetitionDay;
	}


	public String getStartingDate() {
		return startingDate;
	}

	public void setStartingDate(String startingDate) {
		this.startingDate = startingDate;
	}
	
	public String getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(String startingTime) {
		this.startingTime = startingTime;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}


	public int getRepetitionHours() {
		return repetitionHours;
	}

	public void setRepetitionHours(int repetitionHours) {
		this.repetitionHours = repetitionHours;
	}
	
	public int getRepetitionMinutes() {
		return repetitionMinutes;
	}

	public void setRepetitionMinutes(int repetitionMinutes) {
		this.repetitionMinutes = repetitionMinutes;
	}
	
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
}
