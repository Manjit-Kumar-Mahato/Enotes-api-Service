package com.prog.service;

import com.prog.dto.LoginRequest;
import com.prog.dto.LoginResponse;
import com.prog.dto.UserDto;

public interface UserService {

	public Boolean register(UserDto userDto, String url) throws Exception;

	public LoginResponse login(LoginRequest loginRequest);
	
}