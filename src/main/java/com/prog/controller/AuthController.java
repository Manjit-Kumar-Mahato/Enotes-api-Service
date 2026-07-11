package com.prog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog.dto.LoginRequest;
import com.prog.dto.LoginResponse;
import com.prog.dto.UserRequest;
import com.prog.endpoint.AuthEndpoint;
import com.prog.service.AuthService;
import com.prog.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AuthController implements AuthEndpoint{

    @Autowired
    private AuthService authService;

    @Override
    public ResponseEntity<?> registerUser(UserRequest userDto,HttpServletRequest request) throws Exception {
        log.info("Registration request received for email={}", userDto.getEmail());
        String url = CommonUtil.getUrl(request);
        Boolean register = authService.register(userDto, url);
        if (!register) {
            log.error("User registration failed for email={}", userDto.getEmail());
            return CommonUtil.createErrorResponseMessage("Register failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("User registered successfully for email={}", userDto.getEmail());
        return CommonUtil.createBuildResponseMessage("Register success", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) throws Exception {
        log.info("Login request received for email={}", loginRequest.getEmail());
        LoginResponse loginResponse = authService.login(loginRequest);
        if (ObjectUtils.isEmpty(loginResponse)) {
            log.warn("Invalid login attempt for email={}", loginRequest.getEmail());
            return CommonUtil.createErrorResponseMessage("invalid credential", HttpStatus.BAD_REQUEST);
        }
        log.info("User logged in successfully for email={}", loginRequest.getEmail());
        return CommonUtil.createBuildResponse(loginResponse, HttpStatus.OK);
    }
}