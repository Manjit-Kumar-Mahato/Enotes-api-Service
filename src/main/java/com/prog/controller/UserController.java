package com.prog.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog.dto.PasswordChngRequest;
import com.prog.dto.UserResponse;
import com.prog.entity.User;
import com.prog.service.UserService;
import com.prog.util.CommonUtil;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private UserService userService;

	@GetMapping("/profile")
	public ResponseEntity<?> getProfile() {
		User loggedInUser = CommonUtil.getLoggedInUser();
		UserResponse userResponse = mapper.map(loggedInUser, UserResponse.class);
		return CommonUtil.createBuildResponse(userResponse, HttpStatus.OK);
	}

	@PostMapping("/chng-pswd")
	public ResponseEntity<?> changePassword(@RequestBody PasswordChngRequest passwordRequest) {
		userService.changePassword(passwordRequest);
		return CommonUtil.createBuildResponseMessage("Password change success", HttpStatus.OK);
	}

}