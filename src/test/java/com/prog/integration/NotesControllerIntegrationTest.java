package com.prog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prog.dto.LoginRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
public class NotesControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testGetAllNotes() throws Exception {
		String token = generateToken("msugrp@gmail.com", "Demo@000");

		mockMvc.perform(get("/api/v1/notes/").header("Authorization", token)).andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testGetAllNotesByUser() throws Exception {
		String token = generateToken("msugrp@gmail.com", "Demo@000");

		mockMvc.perform(get("/api/v1/notes/user-notes").param("pageNo", "0").param("pageSize", "10")
				.header("Authorization", token)).andExpect(status().isOk());
	}

	@Test
	public void testSearchNotes() throws Exception {
		String token = generateToken("msugrp@gmail.com", "Demo@000");

		mockMvc.perform(get("/api/v1/notes/search").param("key", "Java").param("pageNo", "0").param("pageSize", "10")
				.header("Authorization", token)).andExpect(status().isOk());
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