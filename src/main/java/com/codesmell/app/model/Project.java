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
	private String status;
	private Date lastAnalysis;
	private int totalCommits;
	private int analysedCommits;
	private int interval;
	private Date lastRequest;
	private boolean analysePast;
	private boolean analyseAll;
	private boolean scheduleProject;
	private int countFailedCommits;
	private int countSuccessCommits;
	private Date nextAnalysis;
	private String lastWeekReport;
	
	public Project() 
	{
		this.projectName="New project Name";
		this.creationTime=new Date();
		this.url="";
		this.versionType="git";
		this.analysisStartDate=new Date();
		this.anaysisRepetition="";
//		private String idUser;
//		private String email;
//		private String sonarKey;
//		private String sonarVersion;
//		private String status;
//		private Date lastAnalysis;
//		private int totalCommits;
//		private int analysedCommits;
//		private int interval;
//		private Date lastRequest;
//		private boolean analysePast;
//		private boolean analyseAll;
//		private boolean scheduleProject;
//		private int countFailedCommits;
//		private int countSuccessCommits;
//		private Date nextAnalysis;
//		private String lastWeekReport;
		
		this.creationTime= new Date();
		this.url="";
		
		
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
	
	public String getSonarKey() {
		return sonarKey;
	}

	public void setSonarKey(String sonarKey) {
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
	
	public Date getLastRequest() {
		return  lastRequest;
	}

	public void setLastRequest(Date lastRequest) {
		this.lastRequest = lastRequest;
	}
	
	public String getStatus() {
		return status;
	}
	
	
	public void setLastWeekReport(String lastWeekReport) {
		this.lastWeekReport = lastWeekReport;
	}
	
	public String getLastWeekReport() {
		return this.lastWeekReport;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getLastAnalysis() {
		return lastAnalysis;
	}

	public void setLastAnalysis(Date lastAnalysis) {
		this.lastAnalysis = lastAnalysis;
	}
	
	public void setAnalysePast(boolean analysePast){
		this.analysePast = analysePast;
	}
	public boolean getAnalysePast(){
		return analysePast;
	}
	
	public void setAnalyseAll(boolean analyseAll){
		this.analyseAll = analyseAll;
	}
	public boolean getAnalyseAll(){
		return analyseAll;
	}
	
	public void setInterval(int interval){
		this.interval = interval;
	}
	public int getInterval(){
		return interval;
	}
	
	public void setScheduleProject(boolean scheduleProject){
		this.scheduleProject = scheduleProject;
	}
	public boolean getScheduleProject(){
		return scheduleProject;
	}
	
	public void setCountFailedCommits(int countFailedCommits){
		this.countFailedCommits = countFailedCommits;
	}
	
	public int getCountFailedCommits(){
		return this.countFailedCommits;
	}
	
	public void setCountSuccessCommits(int countSuccessCommits){
		this.countSuccessCommits = countSuccessCommits;
	}
	
	public int getCountSuccessCommits(){
		return this.countSuccessCommits;
	}
	
	public void setNextAnalysis(Date nextAnalysis){
		this.nextAnalysis = nextAnalysis;
	}
	
	public Date getNextAnalysis(){
		return this.nextAnalysis;
	}
	
}
