package com.codesmell.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codesmell.app.directory.UserRepository;
import com.codesmell.app.model.User;


@Controller
class WelcomeController {
	
	@Autowired
	private UserRepository repository;

    @RequestMapping("/")
    public String welcome(Model model) {
    	    model.addAttribute("user", new User()); 
        return "welcome";
    }
    
    @RequestMapping("/newproject")
    public String newProject(Model model) {
        return "newproject";
    }
    
    @PostMapping("/login")    
    public String login(@ModelAttribute User user) {
    	        try{
    			String emailSt = user.getEmail1();
    	        String pwd   = user.getPwd(); 
    	        User usr = repository.findByEmail1(emailSt);
    	        System.out.println(usr.getPwd());
    	        System.out.println(usr.getEmail1());
    	        if (pwd.equals((usr.getPwd()))){
    	        		return "landingPage";
    	        		}
    	        else{
    	        		return "welcome";
    	        }
    	        }
    	        catch(Exception e)
    	        {
    	        	return "welcome";
    	        }
    	    }
}
