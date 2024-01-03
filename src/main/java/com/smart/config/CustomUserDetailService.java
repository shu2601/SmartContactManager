package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Component
public class CustomUserDetailService implements UserDetailsService {

	@Autowired
	private UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


		User user=userRepo.getUserByUserName(username);
	
	if(user == null) {
		
		throw new UsernameNotFoundException("user name not found");
		
	}else
	{
		CustomUser CustomUser= new CustomUser(user);
		  
		 return CustomUser;
		 //return new CustomUser(user);
	}
		
		
		
	
	}

}
