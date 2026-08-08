package com.prog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.prog.dto.CategoryDto;
import com.prog.dto.CategoryResponse;
import com.prog.service.CategoryService;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

	private MockMvc mockMvc;

	@Mock
	private CategoryService categoryService;

	@InjectMocks
	private CategoryController categoryController;

	@BeforeEach
	public void setUp() {

		mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
	}

	@Test
	public void testSaveCategory() throws Exception {

		when(categoryService.saveCategory(any(CategoryDto.class))).thenReturn(true);

		mockMvc.perform(post("/api/v1/category/save").contentType(MediaType.APPLICATION_JSON).content("""
				{
				    "name": "Java Notes",
				    "description": "Java notes",
				    "isActive": true
				}
				""")).andExpect(status().isCreated());

		verify(categoryService).saveCategory(any(CategoryDto.class));
	}

	@Test
	public void testSaveCategoryFailed() throws Exception {

		when(categoryService.saveCategory(any(CategoryDto.class))).thenReturn(false);

		mockMvc.perform(post("/api/v1/category/save").contentType(MediaType.APPLICATION_JSON).content("""
				{
				    "name": "Java Notes",
				    "description": "Java notes",
				    "isActive": true
				}
				""")).andExpect(status().isInternalServerError());

		verify(categoryService).saveCategory(any(CategoryDto.class));
	}

	@Test
	public void testGetAllCategory() throws Exception {

		CategoryDto categoryDto = CategoryDto.builder().id(1).name("Java Notes").description("Java notes")
				.isActive(true).build();

		when(categoryService.getAllCategory()).thenReturn(List.of(categoryDto));

		mockMvc.perform(get("/api/v1/category/")).andExpect(status().isOk());

		verify(categoryService).getAllCategory();
	}

	@Test
	public void testGetAllCategoryEmpty() throws Exception {

		when(categoryService.getAllCategory()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/category/")).andExpect(status().isNoContent());

		verify(categoryService).getAllCategory();
	}

	@Test
	public void testGetActiveCategory() throws Exception {

		CategoryResponse categoryResponse = CategoryResponse.builder().id(1).name("Java").build();

		when(categoryService.getActiveCategory()).thenReturn(List.of(categoryResponse));

		mockMvc.perform(get("/api/v1/category/active")).andExpect(status().isOk());

		verify(categoryService).getActiveCategory();
	}

	@Test
	public void testGetActiveCategoryEmpty() throws Exception {

		when(categoryService.getActiveCategory()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/category/active")).andExpect(status().isNoContent());

		verify(categoryService).getActiveCategory();
	}

	@Test
	public void testGetCategoryById() throws Exception {

		CategoryDto categoryDto = CategoryDto.builder().id(1).name("Java Notes").description("Java notes")
				.isActive(true).build();

		when(categoryService.getCategoryById(1)).thenReturn(categoryDto);

		mockMvc.perform(get("/api/v1/category/1")).andExpect(status().isOk());

		verify(categoryService).getCategoryById(1);
	}

	@Test
	public void testGetCategoryByIdNotFound() throws Exception {

		when(categoryService.getCategoryById(100)).thenReturn(null);

		mockMvc.perform(get("/api/v1/category/100")).andExpect(status().isNotFound());

		verify(categoryService).getCategoryById(100);
	}

	@Test
	public void testDeleteCategoryById() throws Exception {

		when(categoryService.deleteCategory(1)).thenReturn(true);

		mockMvc.perform(delete("/api/v1/category/1")).andExpect(status().isOk());

		verify(categoryService).deleteCategory(1);
	}

	@Test
	public void testDeleteCategoryByIdFailed() throws Exception {

		when(categoryService.deleteCategory(100)).thenReturn(false);

		mockMvc.perform(delete("/api/v1/category/100")).andExpect(status().isInternalServerError());

		verify(categoryService).deleteCategory(100);
	}
}