package com.codesmell.app.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Testing1 implements org.quartz.Job{

	public Testing1() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{
		System.out.println("*********Testing1***************");
	}

}
