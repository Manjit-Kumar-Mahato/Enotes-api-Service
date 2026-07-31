package com.prog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.prog.dto.PswdResetRequest;
import com.prog.endpoint.HomeEndpoint;
import com.prog.service.HomeService;
import com.prog.service.UserService;
import com.prog.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeController implements HomeEndpoint{

	private final HomeService homeService;

	private final UserService userService;

	@Override
	public ResponseEntity<?> verifyUserAccount(Integer uid,String code) throws Exception {
		log.info("HomeController : verifyUserAccount() : Account verification request received. UserId={}", uid);
		Boolean verifyAccount = homeService.verifyAccount(uid, code);
		if (verifyAccount) {
			log.info("HomeController : verifyUserAccount() : Account verified successfully. UserId={}", uid);
			return CommonUtil.createBuildResponseMessage("Account verification success", HttpStatus.OK);
		}
		log.warn("HomeController : verifyUserAccount() : Invalid verification link. UserId={}", uid);
		return CommonUtil.createErrorResponseMessage("Invalid Verification link", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> sendEmailForPasswordReset(String email,HttpServletRequest request) throws Exception {
		log.info("HomeController : sendEmailForPasswordReset() : Password reset email request received. Email={}",email);
		userService.sendEmailPasswordReset(email, request);
		log.info("HomeController : sendEmailForPasswordReset() : Password reset email sent successfully. Email={}",email);
		return CommonUtil.createBuildResponseMessage("Email send Success !! Check your email", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> verifyPasswordResetLink(Integer uid,String code) throws Exception {
		log.info("HomeController : verifyPasswordResetLink() : Password reset link verification request. UserId={}",uid);
		userService.verifyPswdResetLink(uid, code);
		log.info("HomeController : verifyPasswordResetLink() : Password reset link verified successfully. UserId={}",uid);
		return CommonUtil.createBuildResponseMessage("Verification success", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> resetPassword(@RequestBody PswdResetRequest pswdResetRequest) throws Exception {
		log.info("HomeController : resetPassword() : Password reset request received");
		userService.resetPassword(pswdResetRequest);
		log.info("HomeController : resetPassword() : Password reset completed successfully");
		return CommonUtil.createBuildResponseMessage("Password reset success", HttpStatus.OK);
	}

}