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
import com.prog.endpoint.UserEndpoint;
import com.prog.entity.User;
import com.prog.service.UserService;
import com.prog.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UserController implements UserEndpoint{

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private UserService userService;

	@Override
	public ResponseEntity<?> getProfile() {
		log.info("UserController : getProfile() : User profile request received");
		User loggedInUser = CommonUtil.getLoggedInUser();
		UserResponse userResponse = mapper.map(loggedInUser, UserResponse.class);
		log.info("UserController : getProfile() : User profile fetched successfully");
		return CommonUtil.createBuildResponse(userResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> changePassword(PasswordChngRequest passwordRequest) {
		log.info("UserController : changePassword() : Change password request received");
		userService.changePassword(passwordRequest);
		log.info("UserController : changePassword() : Password changed successfully");
		return CommonUtil.createBuildResponseMessage("Password change success", HttpStatus.OK);
	}

}