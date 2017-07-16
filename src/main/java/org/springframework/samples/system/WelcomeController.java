package org.springframework.samples.system;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.sample.module.User;
import org.springframework.samples.directory.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;



@Controller
class WelcomeController {
	
	@Autowired
	private UserRepository repository;

    @RequestMapping("/")
    public String welcome(Model model) {
    	    model.addAttribute("user", new User()); 
        return "welcome";
    }
  
    @PostMapping("/login")    
    public String login(@ModelAttribute User user) {
    	        String emailSt = user.getEmail1();
    	        String pwd   = user.getPwd(); 
    	        User usr = repository.findByEmail1(emailSt);
    	        if (pwd.hashCode() == Integer.parseInt(usr.getPwd()))
    	        		return "OK";
    	        else
    	        		return "welcome";
    	    }

}
