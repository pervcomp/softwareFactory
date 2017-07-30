package com.codesmell.app.schedule;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Schedule {

	@Scheduled(cron="0,09 * * * * *")
	private void createjob()
	{
		Date a = new Date();
		System.out.println(a.toString()+ "Ciao Luca and Amit");
	}
}
