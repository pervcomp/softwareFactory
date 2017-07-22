package com.codesmell.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codesmell.app.dao.UserDao;
import com.codesmell.app.model.User;

@Controller
public class SignUpController {

	@Autowired
	private UserDao userRepository;

	@RequestMapping("/signUp")
	public String welcome(Model model) {
		model.addAttribute("user", new User());
		return "signUp";
	}

	@PostMapping("/registration")
	public String login(@ModelAttribute User user) 
	{
		this.userRepository.save(user);
		return "landingPage";
	}
}
