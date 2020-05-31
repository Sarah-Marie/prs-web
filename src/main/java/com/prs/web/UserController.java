package com.prs.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prs.business.JsonResponse;
import com.prs.business.User;
import com.prs.db.UserRepository;
@CrossOrigin()
@RestController
@RequestMapping("/users")
public class UserController {
	@Autowired
	private UserRepository userRepo;
	
	
	
	
	
	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<User> users = userRepo.findAll();
		if (users.size()==0)  {
			jr = JsonResponse.getErrorInstance("No users found.");
		}
		else  {
			jr = JsonResponse.getInstance(users);
		}
		return jr;
	}
	
	@PostMapping("/login")
	public JsonResponse login(@RequestBody User u)  {
		JsonResponse jr = null;
		
		Optional<User> user = userRepo.findByUserNameAndPassword(u.getUserName(), u.getPassword());
		if (user.isPresent()) {
			jr = JsonResponse.getInstance(user.get());
		}
		else {
			jr = JsonResponse.getErrorInstance("Invalid username/password combination.  Try again.");
		}
		
		return jr;
	}
	
	
	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {
		
		JsonResponse jr = null;
		Optional<User> user = userRepo.findById(id);
		if (user.isPresent())  {
			jr = JsonResponse.getInstance(user.get());
		}
		else {
			jr = JsonResponse.getErrorInstance("No user found for id: "+id);
		}
		
		return jr;
	}
	
	@PostMapping("/")
	public JsonResponse createUser(@RequestBody User u) {
		JsonResponse jr = null;
		
		try {
			u = userRepo.save(u);
			jr = JsonResponse.getInstance(u);
		} 
		
		catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		}
		
		
		catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating user: "+e.getMessage());
			e.printStackTrace();
		}
		
		
		return jr;
		
	}
	
	@PutMapping("/")
	public JsonResponse updateUser(@RequestBody User u) {
JsonResponse jr = null;
		
		try {
			u = userRepo.save(u);
			jr = JsonResponse.getInstance(u);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating user: "+e.getMessage());
			e.printStackTrace();
		}
		
		
		return jr;
		
	}
	
	@DeleteMapping("/{id}")
	public JsonResponse deleteUser(@PathVariable int id) {
JsonResponse jr = null;
		
		try {
			userRepo.deleteById(id);
			jr = JsonResponse.getInstance("User id: " + id + "successfully deleted.");
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting user: "+e.getMessage());
			e.printStackTrace();
		}
		
		
		return jr;
		
	}

}
