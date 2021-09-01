package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	// method for adding comman data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("Username=" + userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER=" + user);
		// get the user using username(Email)
		model.addAttribute("user", user);
	}

	// dashboard Home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String OpenAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";

	}

	// processing add contact home
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session)
	{
		try {
			String name = principal.getName();

			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty())
			{
				//if the file is empty then try our message
				System.out.println("File is Empty");
				contact.setImage("contact.png");
			}
			else
			{
				//upload the file
				contact.setImage(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is Uploaded");
				
			}
			

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("Data=" + contact);

			System.out.println("Added to Database");

			//message success
			session.setAttribute("message", new Message("Your contact is added !! add more..","success"));
			
		} catch (Exception e) {
			System.out.println("Error:"+e.getMessage());
			e.printStackTrace();
			
			//error message
			session.setAttribute("message", new Message("Something went wrong !! Try again.. ","danger"));
		}
		return "normal/add_contact_form";

	}
	
	//show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal)
	{
		model.addAttribute("title", "Show User Contacts");
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		//contact per page 5
		Pageable pageable=PageRequest.of(page, 5);
		
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principle)
	{
		System.out.println("CID="+cId);
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String userName=principle.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "/normal/contact_detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principle)
	{
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		User user=this.userRepository.getUserByUserName(principle.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		
		session.setAttribute("message", new Message("Conatct delete successfully..","success"));
		
		
		
		return "redirect:/user/show-contacts/0";
	}
	

	//Open update form handler
	@PostMapping("/update-contact/{cid}")
	public String UpdateForm(@PathVariable("cid") Integer cid,Model model)
	{
		model.addAttribute("title", "Update Contact");
		
		this.contactRepository.findById(cid);
		
		Contact contact=this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact",contact);
		
		return"normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value="/process-update",method= RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principle)
	{
		
		try {
			
			//fetch old contact details
			Contact oldcontactDetails=this.contactRepository.findById(contact.getcId()).get();
			
			
			if(!file.isEmpty())
			{
				//delete file
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetails.getImage());
				file1.delete();
				
				//update image
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldcontactDetails.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(principle.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated...","success"));
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		System.out.println("Conatct NAME="+contact.getName());
		System.out.println("Contact ID="+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
}
