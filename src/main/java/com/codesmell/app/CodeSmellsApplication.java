package com.codesmell.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class CodeSmellsApplication {

	public static void main(String[] args) { 
		SpringApplication.run(CodeSmellsApplication.class, args);
/*
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler();
			
			JobDetail job1 = newJob(Testing1.class).withIdentity("job1", "group1").build();
			Trigger newTrigger1 = newTrigger().withIdentity("job1", "group1").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(1).repeatForever()).build();

			
			JobDetail job2 = newJob(Testing2.class).withIdentity("job2", "group2").build();
			Trigger newTrigger2 = newTrigger().withIdentity("job2", "group2").startNow()
					.withSchedule(simpleSchedule().withIntervalInSeconds(1).repeatForever()).build();
			
			sched.scheduleJob(job1, newTrigger1);
			sched.scheduleJob(job2, newTrigger2);
			sched.start();
			
			JobKey j1 = new JobKey("job1", "group1");
			
			//TriggerKey t1 = new TriggerKey("job1", "group1");
			sched.deleteJob(j1);
			
			

		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
	}
}
