package com.codesmell.app.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Testing2 implements org.quartz.Job{

	public Testing2() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("*********Testing2******");
		
	}

}
