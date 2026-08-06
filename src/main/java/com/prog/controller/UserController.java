package com.prog.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.prog.dto.PasswordChngRequest;
import com.prog.dto.UserResponse;
import com.prog.endpoint.UserEndpoint;
import com.prog.entity.User;
import com.prog.service.UserService;
import com.prog.util.CommonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserEndpoint{

	private final ModelMapper mapper;

	private final UserService userService;

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