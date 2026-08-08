package com.prog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prog.dto.CategoryDto;
import com.prog.dto.LoginRequest;

@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
public class CategoryControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testSaveCategory() throws Exception {

		CategoryDto categoryDto = CategoryDto.builder().name("Integration Test Category")
				.description("Category created during integration testing").isActive(true).build();

		String token = generateToken("msugrp@gmail.com", "Demo@000");

		mockMvc.perform(post("/api/v1/category/save").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(categoryDto)).header("Authorization", token))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.message").value("saved success"))
				.andExpect(jsonPath("$.status").value("succes"));
	}

	private String generateToken(String email, String password) throws Exception {

		LoginRequest login = new LoginRequest();
		login.setEmail(email);
		login.setPassword(password);

		String response = mockMvc
				.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(login)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		JsonNode root = objectMapper.readTree(response);
		String token = root.path("data").path("token").asText();

		return "Bearer " + token;
	}
}