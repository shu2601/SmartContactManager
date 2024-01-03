package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller

public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home() {

		return "home";

	}
	//handler for about page
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "This is about page");

		return "about";
	}
	
	//handler for login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title", "Login Page");
		
		return "login";
	}
	
	
	
	
	
	
	
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		
		model.addAttribute("title", "Registration page");
		model.addAttribute("user", new User());
		return "signup";

	}
	
	//this handler for register user
	
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user")User user, BindingResult result1,@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session ) {
		
		try {
			
			if(!agreement) {
				System.out.println("you have not aggreed terms and condition");
				throw new Exception("you have not aggreed terms and condition");
			}
			
			if(result1.hasErrors()) {
				
				System.out.println("Error"+result1.toString());
				model.addAttribute("user", user);
				return"signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageurl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("Agreement"+agreement);
			System.out.println(user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("Registration successful! You are now a registered user.", "alert-success" ));
			return "signup";
			
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Somthing Wrong!! "+e.getMessage(), "alert-danger" ));
			return "signup";
		
		}
		
		
		
	}

}
