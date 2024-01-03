package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ContactRepository contactRepositoy;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		
		String userName=principal.getName();
		System.out.println(userName);
		
		User user= 	userRepo.getUserByUserName(userName);
		
		model.addAttribute("user", user);
		
		
		
	}

	
	@RequestMapping("/index")
	public String dashBoard(Model model, Principal principal) {
		
		model.addAttribute("title", "user dashboard");
		String userNname = principal.getName();
		System.err.println("Name of loggin user "+userNname);
		//get user data using username
		
		User user = userRepo.getUserByUserName(userNname);
		
		model.addAttribute("user", user);
		System.out.println(user);
		
		return "normal/user_dashboard";
	}
	
	//open contact page
	
	@GetMapping("/contact")
	public String openAddContactForm(Model model, Principal principal) {
		
		model.addAttribute("title", "contact page");
		String name = principal.getName();
		User user = userRepo.getUserByUserName(name);
		
		model.addAttribute("user", user);
		model.addAttribute("contact", new Contact());
		
		
		
		return "normal/add_contact_form";
	}
	
	
	//submit or process contact details in database
	
	@PostMapping("/process-contact")
	public String addContact(@Valid	@ModelAttribute ("contact") Contact contact, BindingResult result,@RequestParam ("imageFile")MultipartFile file, Principal principal, Model model,HttpSession session) {
		
	
		try {
			
			if(result.hasErrors()) {
				
			
				model.addAttribute("contact", contact);
				return "normal/add_contact_form";
			}
		
			
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		
		if(file.isEmpty()) {
			
			//file is empty
			System.out.println();
			contact.setImage("contact.png");
			
		}else {
			//file is not empty
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/img").getFile();
			
			
		Path path=	Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.err.println("Image is uploaded!!");
			
		}
	
		
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepo.save(user);
		
		model.addAttribute("contact", new Contact());
		
		System.err.println("Contact details : "+contact);
		System.out.println("Contact Added succesfully!!	");
		session.setAttribute("message", new Message("Contact added successfully!! Add more!!", "alert-success" ));
		
		}
		
		 catch (Exception e) {
				
			 System.out.println("Error"+e.getMessage());
			 e.printStackTrace();
			model.addAttribute("contact", contact);
			 session.setAttribute("message", new Message("Somthing Wrong!! "+e.getMessage(), "alert-danger" ));
				
			}
		return "normal/add_contact_form";
	}
	//view contact
	
	
	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page ,Model model, Principal principal) {
		
		model.addAttribute("title", "View User Contact");
		List<Contact> contact = contactRepositoy.findAll();
		
		String userName = principal.getName();
	User user = this.userRepo.getUserByUserName(userName);
	
		Pageable pageable = PageRequest.of(page, 4);
		
		Page<Contact> contacts = this.contactRepositoy.findContactByUser(user.getId(),pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	//showing perticuler contact detail
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		model.addAttribute("title", "personal contact page");
		
		System.out.println("CID"+cId);
		Optional<Contact> contactOptional = this.contactRepositoy.findById(cId);
		Contact contact = contactOptional.get();
		
		//get user
		String userName = principal.getName();
		User user = this.userRepo.getUserByUserName(userName);
	
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		
		return "normal/contact_detail";
	}
	//delete contact by id
	
	@GetMapping("/delete/{cid}")
	public String delete(@PathVariable("cid") Integer cId,Model model, HttpSession session) 
	{
		
		Contact contact = this.contactRepositoy.findById(cId).get();
		//Contact contact = contactOptional.get();
		
		contact.setUser(null);
		
		
		
		this.contactRepositoy.delete(contact);
		System.out.println("deleted");
		session.setAttribute("message", new Message("Contact deleted successfully!!", "alert-success"));
		
		
		return "redirect:/user/show-contact/0";
		
		
	}
	//update form
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid, Model model) {
		
		model.addAttribute("title", "Update Contact");
		
		
	Contact contact=	this.contactRepositoy.findById(cid).get();
	
	model.addAttribute("contact", contact);
	
		return "normal/update_form";
	}
	
	
	// update process handler

	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("imageFile") MultipartFile file,
			Model model, HttpSession session, Principal principal) {

		try {
			// image
			// old contact details

			Contact oldcontactDetail = this.contactRepositoy.findById(contact.getcId()).get();

			if (!file.isEmpty()) {
				// file work, rewrite
				// delete file
				// *****************update file****************
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());

			} else {

				contact.setImage(oldcontactDetail.getImage());
			}

			User user = this.userRepo.getUserByUserName(principal.getName());

			contact.setUser(user);
			this.contactRepositoy.save(contact);

			session.setAttribute("message", new Message("Contact is updated successfully!!", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		session.setAttribute("message", new Message("Contact is updated successfully!!", "alert-success"));

		return "normal/contact_detail";
		// return "redirect:/user/show-contact/0";
	}
	
	//My profile
	
	@RequestMapping("/my-profile")
	public String myProfile(Model model, Principal principal) {
		
		model.addAttribute("title", "User Profile");
		String userNname = principal.getName();
		System.err.println("Name of loggin user "+userNname);
		//get user data using username
		
		User user = userRepo.getUserByUserName(userNname);
		
		model.addAttribute("user", user);
		System.out.println(user);
		
		return "normal/my_profile";
	}
	
	
	
}
