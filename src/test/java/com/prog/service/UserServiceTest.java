package com.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.prog.dto.EmailRequest;
import com.prog.dto.PasswordChngRequest;
import com.prog.dto.PswdResetRequest;
import com.prog.entity.AccountStatus;
import com.prog.entity.User;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.UserRepository;
import com.prog.service.impl.EmailService;
import com.prog.service.impl.UserServiceImpl;
import com.prog.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserRepository userRepo;

	@Mock
	private EmailService emailService;

	@Mock
	private AccountStatus accountStatus;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private UserServiceImpl userService;

	private User user;

	@BeforeEach
	public void initialize() {

		user = new User();

		user.setId(1);
		user.setFirstName("Manjit");
		user.setLastName("Kumar");
		user.setEmail("test@gmail.com");
		user.setPassword("encodedOldPassword");
		user.setStatus(accountStatus);
	}

	@Test
	public void testChangePassword() {

		PasswordChngRequest passwordRequest = mock(PasswordChngRequest.class);

		when(passwordRequest.getOldPassword()).thenReturn("oldPassword");

		when(passwordRequest.getNewPassword()).thenReturn("newPassword");

		when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

		when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			userService.changePassword(passwordRequest);

			assertEquals("encodedNewPassword", user.getPassword());

			verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");

			verify(passwordEncoder).encode("newPassword");

			verify(userRepo).save(user);
		}
	}

	@Test
	public void testChangePasswordWrongOldPassword() {

		PasswordChngRequest passwordRequest = mock(PasswordChngRequest.class);

		when(passwordRequest.getOldPassword()).thenReturn("wrongPassword");

		when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

		try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

			mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> userService.changePassword(passwordRequest));

			assertEquals("Old Password is incorrect !!", exception.getMessage());

			verify(passwordEncoder).matches("wrongPassword", "encodedOldPassword");

			verify(passwordEncoder, never()).encode(any());

			verify(userRepo, never()).save(any(User.class));
		}
	}

	@Test
	public void testVerifyPswdResetLink() throws Exception {

		when(userRepo.findById(1)).thenReturn(Optional.of(user));

		when(accountStatus.getPasswordResetToken()).thenReturn("valid-token");

		userService.verifyPswdResetLink(1, "valid-token");

		verify(userRepo).findById(1);

		verify(accountStatus).getPasswordResetToken();
	}

	@Test
	public void testVerifyPswdResetLinkInvalidUser() {

		when(userRepo.findById(100)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> userService.verifyPswdResetLink(100, "valid-token"));

		assertEquals("invalid user", exception.getMessage());

		verify(userRepo).findById(100);
	}

	@Test
	public void testVerifyPswdResetLinkAlreadyReset() {

		when(userRepo.findById(1)).thenReturn(Optional.of(user));

		when(accountStatus.getPasswordResetToken()).thenReturn(null);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> userService.verifyPswdResetLink(1, "valid-token"));

		assertEquals("Already Password reset", exception.getMessage());
	}

	@Test
	public void testVerifyPswdResetLinkInvalidCode() {

		when(userRepo.findById(1)).thenReturn(Optional.of(user));

		when(accountStatus.getPasswordResetToken()).thenReturn("correct-token");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> userService.verifyPswdResetLink(1, "wrong-token"));

		assertEquals("invalid url", exception.getMessage());
	}

	@Test
	public void testVerifyPswdResetLinkEmptyCode() {

		when(userRepo.findById(1)).thenReturn(Optional.of(user));

		when(accountStatus.getPasswordResetToken()).thenReturn("correct-token");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> userService.verifyPswdResetLink(1, ""));

		assertEquals("invalid token", exception.getMessage());
	}

	@Test
	public void testResetPassword() throws Exception {

		PswdResetRequest resetRequest = mock(PswdResetRequest.class);

		when(resetRequest.getUid()).thenReturn(1);

		when(resetRequest.getNewPassword()).thenReturn("newPassword");

		when(userRepo.findById(1)).thenReturn(Optional.of(user));

		when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

		userService.resetPassword(resetRequest);

		assertEquals("encodedNewPassword", user.getPassword());

		verify(passwordEncoder).encode("newPassword");

		verify(accountStatus).setPasswordResetToken(null);

		verify(userRepo).save(user);
	}

	@Test
	public void testResetPasswordInvalidUser() {

		PswdResetRequest resetRequest = mock(PswdResetRequest.class);

		when(resetRequest.getUid()).thenReturn(100);

		when(userRepo.findById(100)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
				() -> userService.resetPassword(resetRequest));

		assertEquals("invalid user", exception.getMessage());

		verify(userRepo).findById(100);

		verify(passwordEncoder, never()).encode(any());

		verify(userRepo, never()).save(any(User.class));
	}
}