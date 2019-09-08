package com.codesmell.app.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codesmell.app.dao.CommitAnalysisDao;
import com.codesmell.app.dao.CommitDao;
import com.codesmell.app.dao.CommitErrorDao;
import com.codesmell.app.dao.ProjectDao;
import com.codesmell.app.dao.ScheduleDao;
import com.codesmell.app.dao.SonarServerDao;
import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.Commit;
import com.codesmell.app.model.CommitAnalysis;
import com.codesmell.app.model.Project;
import com.codesmell.app.model.Schedule;
import com.codesmell.app.model.SonarServer;
import com.codesmell.app.model.User;

import code.codesmell.app.controllerUtilities.ControllerUtilities;

@Controller
class WelcomeController {

	@Autowired
	private UserDao userDao;
	@Autowired
	private CommitAnalysisDao commitAnalysisDao;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private CommitDao commitDao;
	@Autowired
	private ScheduleDao scheduleDao;
	@Autowired
	private CommitErrorDao commitErrorDao;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private SonarServerDao sonarServerDao;


	@RequestMapping("/")
	public String welcome(@CookieValue(value = "email", defaultValue = "") String email, Model model,
			HttpServletRequest req, HttpServletResponse resp) {
	    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
		if (req.getSession().getAttribute("email") != null) {
			String emailSession = (String) req.getSession().getAttribute("email");
			cu.configureModelLandingPage(model, emailSession);
			model.addAttribute("user", new User());
			return "landingPage";
		} else if (!email.isEmpty() && !email.equals("null")) {
			req.getSession().setAttribute("email", email);
			cu.configureModelLandingPage(model, email);
	
			return "landingPage";
		}
		model.addAttribute("user", new User());
		return "welcome";
	}

	@RequestMapping("/newproject")
	public String newProject(Model model, HttpServletRequest req, HttpServletResponse resp) {
		/*if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			} TO UNCOMMENT
			*/ 
		model.addAttribute("email", req.getSession().getAttribute("email"));
		model.addAttribute("project", new Project());
		model.addAttribute("schedule", new Schedule());
		
		return "newproject";
	}
	
	@RequestMapping("/conf")
	public String sonarConf(Model model, HttpServletRequest req, HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		model.addAttribute("email", req.getSession().getAttribute("email"));
		List<SonarServer> list = sonarServerDao.findAllByOrderBySonarDateDesc();
		if (list.isEmpty()){
			SonarServer ss = new SonarServer();
			sonarServerDao.save(ss);
			model.addAttribute("server", ss);
			}
		else
			model.addAttribute("server", list.get(0));
		return "conf";
	}
	
	@PostMapping("/updateServer")
	public String updateServer(Model model, @ModelAttribute SonarServer server, HttpServletRequest req, HttpServletResponse resp) {
		sonarServerDao.save(server);
		model.addAttribute("email", req.getSession().getAttribute("email"));
		model.addAttribute("server",server);
		model.addAttribute("OK","OK");
		updateServerProperties(server.getSonarServerUrl());
		return "conf";
	}
	
	private void updateServerProperties(String url){
		File currentDirFile = new File(".");
		List<File> toDelete = new LinkedList<File>();
		for (File f : currentDirFile.listFiles()){
			if (f.getName().contains(".properties")){
				toDelete.add(f);
				PrintWriter pw;
				List<String> lines;
				try {
					pw = new PrintWriter(f.getName()+"2");
					lines = Files.readAllLines(f.toPath());
					for (String line : lines){
						if (line.contains("sonar.host.url=")){
							pw.println("sonar.host.url="+url);
						}
						else{
							pw.println(line);
						}
					}
					pw.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		
			}
		}
		for (int i = 0; i< toDelete.size();i++){
			String name = toDelete.get(i).getName();
			toDelete.get(i).delete();
			File temp = new File(name+"2");
			temp.renameTo(new File(name));
		}
		List<Project> projects = this.projectDao.findAll();
		for (Project p : projects){
			p.setSonarHost(url);
			projectDao.save(p);
		}
		
	}
	

	@RequestMapping("/landingPage")
	public String landingPaget(Model model, HttpServletRequest req, HttpServletResponse resp,@CookieValue(value = "email", defaultValue = "") String email) {
	    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
		if (req.getSession().getAttribute("email") != null) {
			String emailSession = (String) req.getSession().getAttribute("email");
			cu.configureModelLandingPage(model, emailSession);
			return "landingPage";
		} else if (!email.isEmpty()) {
			req.getSession().setAttribute("email", email);
			cu.configureModelLandingPage(model, email);
			//cu.scheduleDailyReport(userDao.findByEmail1((String)req.getSession().getAttribute("email")), mailSender);
			cu.configureModelLandingPage(model, email);
			return "landingPage";
		}
		else {
			String emailSession = (String) req.getSession().getAttribute("email");
			if (req.getSession().getAttribute("email") == null) {
				return "welcome";
				}
			cu.configureModelLandingPage(model, emailSession);
			//cu.scheduleDailyReport(userDao.findByEmail1((String)req.getSession().getAttribute("email")), mailSender);
			cu.configureModelLandingPage(model, emailSession);
			return "landingPage";
		}
	}
	
	/**
	 * Prepares the projectDetails page
	 * @param model
	 * @param projectToSend
	 * @param req
	 * @param resp
	 * @return
	 */
	@PostMapping("/projectDet")
	public String projectDetails(Model model, @ModelAttribute Project projectToSend,HttpServletRequest req, HttpServletResponse resp) {
		if (req.getSession().getAttribute("email") == null) {
			return "welcome";
			}
		
		ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
		String projectName = projectToSend.getProjectName();
		Project p = projectDao.findByprojectName(projectName);
		cu.getUpdateProject(p);		
		cu.configureModelDetailsPage(model,(String)req.getSession().getAttribute("email"),p);
		return "projectDetails";
	}
	
	@RequestMapping("/logout")
	public String logout(Model model, HttpServletRequest req, HttpServletResponse resp) {
		req.getSession().removeAttribute("" + "email");
		Cookie cookie = new Cookie("email", null); // Not necessary, but saves
													// bandwidth.
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0); // Don't set to -1 or it will become a session
								// cookie!
		resp.addCookie(cookie);
		model.addAttribute("user", new User());
	
		return "welcome";
	}

	@PostMapping("/login")
	public String login(Model model, @ModelAttribute User user, HttpServletRequest req, HttpServletResponse resp) {
		try {
			if (user ==  null)
				return "welcome";
			else if (user.getEmail1().isEmpty() || user.getPwd().isEmpty())
				return "welcome";
					
			//String emailSt = user.getEmail1();
			//String pwd = user.getPwd();
			//User usr = userDao.findByEmail1(emailSt);
			//if (pwd.equals((usr.getPwd()))) {
			    ControllerUtilities cu = new ControllerUtilities(projectDao, commitAnalysisDao, commitDao, userDao, scheduleDao,commitErrorDao);
				req.getSession().setAttribute("email", "admin");
				resp.addCookie(new Cookie("email", "admin"));
				cu.configureModelLandingPage(model, "admin");
				return "landingPage";
			/*} else {
				return "welcome";
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			return "welcome";
		}
	}
	


}
