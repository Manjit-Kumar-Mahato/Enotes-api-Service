package com.prog.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prog.entity.AccountStatus;
import com.prog.entity.User;
import com.prog.exception.ResourceNotFoundException;
import com.prog.exception.SuccessException;
import com.prog.repository.UserRepository;
import com.prog.service.HomeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HomeServiceImpl implements HomeService {

	@Autowired
	private UserRepository userRepo;

	@Override
	public Boolean verifyAccount(Integer userId, String verificationCode) throws Exception {
		log.info("HomeServiceImpl : verifyAccount() : Start");
		User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("invalid user"));

		if(user.getStatus().getVerificationCode()==null)
		{
			log.info("message : Account alreday verified");
			throw new SuccessException("Account alreday verified");
		}
		
		if (user.getStatus().getVerificationCode().equals(verificationCode)) {
			AccountStatus status = user.getStatus();
			status.setIsActive(true);
			status.setVerificationCode(null);
			userRepo.save(user);
			log.info("message : Account verification success");
			return true;
		}
		log.info("HomeServiceImpl : verifyAccount() : End");
		return false;
	}

}