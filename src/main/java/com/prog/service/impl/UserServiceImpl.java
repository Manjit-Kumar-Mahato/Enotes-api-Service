package com.prog.service.impl;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.prog.dto.EmailRequest;
import com.prog.dto.PasswordChngRequest;
import com.prog.dto.PswdResetRequest;
import com.prog.entity.User;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.UserRepository;
import com.prog.service.UserService;
import com.prog.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	
	private final PasswordEncoder passwordEncoder;
	
	private final UserRepository userRepo;
	
	private final EmailService emailService;

	@Override
	public void changePassword(PasswordChngRequest passwordRequest) {

		User logedInUser = CommonUtil.getLoggedInUser();

		log.info("UserServiceImpl : changePassword() : Password change request. UserId={}", logedInUser.getId());

		if (!passwordEncoder.matches(passwordRequest.getOldPassword(), logedInUser.getPassword())) {
			log.warn("UserServiceImpl : changePassword() : Invalid old password. UserId={}", logedInUser.getId());
			throw new IllegalArgumentException("Old Password is incorrect !!");
		}

		String encodePassword = passwordEncoder.encode(passwordRequest.getNewPassword());
		logedInUser.setPassword(encodePassword);
		userRepo.save(logedInUser);

		log.info("UserServiceImpl : changePassword() : Password changed successfully. UserId={}",
				logedInUser.getId());
	}

	@Override
	public void sendEmailPasswordReset(String email, HttpServletRequest request) throws Exception {

		log.info("UserServiceImpl : sendEmailPasswordReset() : Password reset email request. Email={}", email);

		User user = userRepo.findByEmail(email);

		if (ObjectUtils.isEmpty(user)) {
			log.warn("UserServiceImpl : sendEmailPasswordReset() : User not found. Email={}", email);
			throw new ResourceNotFoundException("Invalid Email");
		}

		String passwordResetToken = UUID.randomUUID().toString();
		user.getStatus().setPasswordResetToken(passwordResetToken);

		User updateUser = userRepo.save(user);

		String url = CommonUtil.getUrl(request);

		sendEmailRequest(updateUser, url);

		log.info("UserServiceImpl : sendEmailPasswordReset() : Password reset email sent. Email={}", email);
	}

	private void sendEmailRequest(User user, String url) throws Exception {

		log.info("UserServiceImpl : sendEmailRequest() : Preparing password reset email. UserId={}",
				user.getId());

		String message = "Hi <b>[[username]]</b> "
				+ "<br><p>You have requested to reset your password.</p>"
				+ "<p>Click the link below to change your password:</p>"
				+ "<p><a href=[[url]]>Change my password</a></p>"
				+ "<p>Ignore this email if you do remember your password, "
				+ "or you have not made the request.</p><br>"
				+ "Thanks,<br>Enotes.com";

		message = message.replace("[[username]]", user.getFirstName());
		message = message.replace("[[url]]",
				url + "/api/v1/home/verify-pswd-link?uid=" + user.getId()
						+ "&&code=" + user.getStatus().getPasswordResetToken());

		EmailRequest emailRequest = EmailRequest.builder()
				.to(user.getEmail())
				.title("Password Reset")
				.subject("Password Reset Link")
				.message(message)
				.build();

		emailService.sendEmail(emailRequest);

		log.info("UserServiceImpl : sendEmailRequest() : Password reset email prepared successfully. UserId={}",
				user.getId());
	}

	@Override
	public void verifyPswdResetLink(Integer uid, String code) throws Exception {

		log.info("UserServiceImpl : verifyPswdResetLink() : Verifying password reset link. UserId={}", uid);

		User user = userRepo.findById(uid)
				.orElseThrow(() -> new ResourceNotFoundException("invalid user"));

		verifyPasswordResetToken(user.getStatus().getPasswordResetToken(), code);

		log.info("UserServiceImpl : verifyPswdResetLink() : Password reset link verified successfully. UserId={}",
				uid);
	}

	private void verifyPasswordResetToken(String existToken, String reqToken) {

		if (StringUtils.hasText(reqToken)) {

			if (!StringUtils.hasText(existToken)) {
				throw new IllegalArgumentException("Already Password reset");
			}

			if (!existToken.equals(reqToken)) {
				throw new IllegalArgumentException("invalid url");
			}

		} else {
			throw new IllegalArgumentException("invalid token");
		}
	}

	@Override
	public void resetPassword(PswdResetRequest pswdResetRequest) throws Exception {

		log.info("UserServiceImpl : resetPassword() : Reset password request. UserId={}",
				pswdResetRequest.getUid());

		User user = userRepo.findById(pswdResetRequest.getUid())
				.orElseThrow(() -> new ResourceNotFoundException("invalid user"));

		String encodePassword = passwordEncoder.encode(pswdResetRequest.getNewPassword());

		user.setPassword(encodePassword);
		user.getStatus().setPasswordResetToken(null);

		userRepo.save(user);

		log.info("UserServiceImpl : resetPassword() : Password reset successfully. UserId={}",
				user.getId());
	}

}