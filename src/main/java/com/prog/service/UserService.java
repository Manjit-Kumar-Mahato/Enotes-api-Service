package com.prog.service;

import com.prog.dto.UserDto;

public interface UserService {

	public Boolean register(UserDto userDto)throws Exception;
	
}