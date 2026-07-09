package com.prog.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prog.entity.AccountStatus;
import com.prog.entity.User;
import com.prog.exception.ResourceNotFoundException;
import com.prog.exception.SuccessException;
import com.prog.repository.UserRepository;
import com.prog.service.HomeService;

@Component
public class HomeServiceImpl implements HomeService {

	@Autowired
	private UserRepository userRepo;

	@Override
	public Boolean verifyAccount(Integer userId, String verificationCode) throws Exception {

		User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("invalid user"));

		if(user.getStatus().getVerificationCode()==null)
		{
			throw new SuccessException("Account alreday verified");
		}
		
		if (user.getStatus().getVerificationCode().equals(verificationCode)) {
			AccountStatus status = user.getStatus();
			status.setIsActive(true);
			status.setVerificationCode(null);

			userRepo.save(user);

			return true;
		}

		return false;
	}

}