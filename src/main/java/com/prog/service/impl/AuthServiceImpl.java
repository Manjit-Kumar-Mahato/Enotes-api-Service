package com.prog.service.impl;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.prog.config.security.CustomUserDetails;
import com.prog.dto.EmailRequest;
import com.prog.dto.LoginRequest;
import com.prog.dto.LoginResponse;
import com.prog.dto.UserRequest;
import com.prog.dto.UserResponse;
import com.prog.entity.AccountStatus;
import com.prog.entity.Role;
import com.prog.entity.User;
import com.prog.repository.RoleRepository;
import com.prog.repository.UserRepository;
import com.prog.service.JwtService;
import com.prog.service.AuthService;
import com.prog.util.Validation;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private Validation validation;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private EmailService emailService;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtService jwtService;

	@Override
	public Boolean register(UserRequest userDto, String url) throws Exception {

		validation.userValidation(userDto);
		User user = mapper.map(userDto, User.class);

		setRole(userDto, user);

		AccountStatus status = AccountStatus.builder().isActive(false).verificationCode(UUID.randomUUID().toString())
				.build();
		user.setStatus(status);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User saveUser = userRepo.save(user);
		if (!ObjectUtils.isEmpty(saveUser)) {
			// send email
			emailSend(saveUser, url);
			return true;
		}
		return false;
	}

	private void emailSend(User saveUser, String url) throws Exception {

		String message = "Hi,<b>[[username]]</b> " + "<br> Your account register sucessfully.<br>"
				+ "<br> Click the below link verify & Active your account <br>"
				+ "<a href='[[url]]'>Click Here</a> <br><br>" + "Thanks,<br>Enotes.com";

		message = message.replace("[[username]]", saveUser.getFirstName());
		message = message.replace("[[url]]", url + "/api/v1/home/verify?uid=" + saveUser.getId() + "&&code="
				+ saveUser.getStatus().getVerificationCode());

		EmailRequest emailRequest = EmailRequest.builder().to(saveUser.getEmail())
				.title("Account Creating Confirmation").subject("Account Created Success").message(message).build();
		emailService.sendEmail(emailRequest);
	}

	private void setRole(UserRequest userDto, User user) {
		List<Integer> reqRoleId = userDto.getRoles().stream().map(r -> r.getId()).toList();
		List<Role> roles = roleRepo.findAllById(reqRoleId);
		user.setRoles(roles);
	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {

		Authentication authenticate = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		if(authenticate.isAuthenticated())
		{
			CustomUserDetails customUserDetails= 
					(CustomUserDetails)authenticate.getPrincipal();
			
			String token=jwtService.generateToken(customUserDetails.getUser());
			
			LoginResponse loginResponse=LoginResponse.builder()
					.user(mapper.map(customUserDetails.getUser(), UserResponse.class))
					.token(token)
					.build();
			return loginResponse;
		}
		
		return null;
	}

}