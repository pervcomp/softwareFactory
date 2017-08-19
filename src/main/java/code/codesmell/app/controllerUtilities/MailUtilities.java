package code.codesmell.app.controllerUtilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.model.CommitError;
import com.codesmell.app.model.User;

public class MailUtilities implements org.quartz.Job {
	private User user;
	private JavaMailSender mailSender;
	private CommitErrorDao commitErrorDao;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
	    SchedulerContext context = null;

		try {
			context = arg0.getScheduler().getContext();
		} catch (SchedulerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		this.mailSender = (JavaMailSender) context.get("mailSender");
		this.user = (User) context.get("user");
		this.commitErrorDao = (CommitErrorDao) context.get("commitErrorDao");
		
		MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
		try {
			String message = getMessage();
			if (message == null)
				return;
			messageHelper = new MimeMessageHelper(mail, true);
	        messageHelper.setTo(user.getEmail1());
	        messageHelper.setSubject("CodeSmells Report Failed Analysis");
	        messageHelper.setText(message, true);
	        mailSender.send(mail);
	       
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getMessage(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);  // number of days to add
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        Date start = c.getTime();
        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, -1);  // number of days to add
        c2.set(Calendar.HOUR_OF_DAY, 23);
        c2.set(Calendar.MINUTE, 59);
        Date end = c2.getTime();
        
		List<CommitError> errorsOfDay = commitErrorDao.findByEmailAndErrorDateBetweenOrderByErrorDateDesc(user.getEmail1(), start, end);
		if (errorsOfDay.isEmpty())
			return null;
		String message = "<html>";
		for (CommitError ce : errorsOfDay){
		    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyy HH:mm");
		    message +=" <strong>Project: </strong>"+ce.getProjectName()+"<br>"   +
				      " <strong>Analysis: </strong>"+ ce.getAnalysisId() + "<br>"+
				      " <strong>Date: </strong> "+ format.format(ce.getDate()) + "<br>" + 
				      " <strong>Commit: </strong>"+ce.getIdCommit()+"<br>";
            message += " <details>";
            String errMessage = ce.getErrorMessage();
            errMessage = errMessage.replace("\n", "<br>");
            errMessage = errMessage.replace("\tat", "&nbsp&nbsp&nbsp");
            errMessage = errMessage.replace("\t", "&nbsp");
		    message += errMessage;
            message += "</details>";
            message += "<br><br>";
		}
		message += "</html>";
		return message;
	}
	
	
	
	
}
