package com.prog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prog.entity.User;
import java.util.List;


public interface UserRepository extends JpaRepository<User,Integer>{
	
	 Boolean existsByEmail(String email);
	 
	 User findByEmail(String email); 
}
