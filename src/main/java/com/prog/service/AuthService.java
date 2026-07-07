package com.prog.service;

import com.prog.dto.LoginRequest;
import com.prog.dto.LoginResponse;
import com.prog.dto.UserRequest;

public interface AuthService {

	public Boolean register(UserRequest userDto, String url) throws Exception;

	public LoginResponse login(LoginRequest loginRequest);
	
}