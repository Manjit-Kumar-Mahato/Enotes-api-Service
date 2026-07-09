package com.prog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog.service.HomeService;
import com.prog.dto.PswdResetRequest;
import com.prog.service.UserService;
import com.prog.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

	@Autowired
	private HomeService homeService;
	
	@Autowired
	private UserService userService;

	@GetMapping("/verify")
	public ResponseEntity<?> verifyUserAccount(@RequestParam Integer uid, @RequestParam String code) throws Exception {
		Boolean verifyAccount = homeService.verifyAccount(uid, code);
		if (verifyAccount)
			return CommonUtil.createBuildResponseMessage("Account verification success", HttpStatus.OK);
		return CommonUtil.createErrorResponseMessage("Invalid Verification link", HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping("/send-email-reset")
	public ResponseEntity<?> sendEmailForPasswordReset(@RequestParam String email,HttpServletRequest request) throws Exception{
		userService.sendEmailPasswordReset(email,request);
		return CommonUtil.createBuildResponseMessage("Email send Success !! Check your email", HttpStatus.OK);
	}
	
	@GetMapping("/verify-pswd-link")
	public ResponseEntity<?> verifyPasswordResetLink(@RequestParam Integer uid, @RequestParam String code)
			throws Exception {
		userService.verifyPswdResetLink(uid, code);
		return CommonUtil.createBuildResponseMessage("verification success", HttpStatus.OK);
	}

	@PostMapping("/reset-pswd")
	public ResponseEntity<?> resetPassword(@RequestBody PswdResetRequest pswdResetRequest) throws Exception {
		userService.resetPassword(pswdResetRequest);
		return CommonUtil.createBuildResponseMessage("Password reset succes", HttpStatus.OK);
	}

}