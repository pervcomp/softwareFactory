package com.codesmell.app;

import static org.quartz.JobBuilder.*;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

import com.codesmell.app.schedule.Testing1;
import com.codesmell.app.schedule.Testing2;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class CodeSmellsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeSmellsApplication.class, args);

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
			
			TriggerKey t1 = new TriggerKey("job1", "group1");
			sched.unscheduleJob(t1);
			
			

		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
