package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home-Smart Contact Manger");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manger");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manger");
		model.addAttribute("user", new User());
		return "signup";
	}

	// this handler for register user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,HttpSession session) {
		try {
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR "+ result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnable(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			

			System.out.println("Agreement " + agreement);
			System.out.println("User " + user);

			User result = this.userRepository.save(user);

			model.addAttribute("user", new User());
			
			session.setAttribute("message",new Message("Sucessfully Registerd !!","alert-success"));
			return "signup";
			
		} catch (Exception e) {
		
			e.printStackTrace();
			session.setAttribute("message",new Message("Something Went Wrong !!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","Login Page");
		return "login.html";
	}
	
}
