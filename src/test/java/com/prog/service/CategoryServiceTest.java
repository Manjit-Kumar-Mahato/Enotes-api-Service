package com.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.prog.dto.CategoryDto;
import com.prog.dto.CategoryResponse;
import com.prog.entity.Category;
import com.prog.exception.ExistDataException;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.CategoryRepository;
import com.prog.service.impl.CategoryServiceImpl;
import com.prog.util.Validation;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepo;

	@InjectMocks
	private CategoryServiceImpl categoryService;

	@Mock
	private Validation validation;

	private CategoryDto categoryDto = null;
	private Category category = null;
	private List<Category> categories = new ArrayList<>();
	private List<CategoryDto> categoriesDto = new ArrayList<>();

	@Mock
	private ModelMapper mapper;

	@BeforeEach
	public void initalize() {
		categoryDto = CategoryDto.builder().id(null).name("Java Notes").description("java notes").isActive(true)
				.build();

		category = Category.builder().id(null).name("Java Notes").description("java notes").isActive(true)
				.isDeleted(false).build();

		categories.add(category);
		categoriesDto.add(categoryDto);

	}

	@Test
 	public void testSaveCategory() {
		// arrange
		when(categoryRepo.existsByName(categoryDto.getName())).thenReturn(false);
		when(mapper.map(categoryDto, Category.class)).thenReturn(category);
		when(categoryRepo.save(category)).thenReturn(category);

		// act
		Boolean saveCategory = categoryService.saveCategory(categoryDto);

		// assert
		assertTrue(saveCategory);

		// verify
		verify(validation).categoryValidation(categoryDto);
		verify(categoryRepo).existsByName(categoryDto.getName());
		verify(categoryRepo).save(category);
	}
	
	@Test
	public void testCategoryExist() {
		when(categoryRepo.existsByName(categoryDto.getName())).thenReturn(true);
		ExistDataException exception = assertThrows(ExistDataException.class, ()->{
			categoryService.saveCategory(categoryDto);
		});
		assertEquals("Category already exist", exception.getMessage());
		verify(validation).categoryValidation(categoryDto);
		verify(categoryRepo).existsByName(categoryDto.getName());
		verify(categoryRepo,never()).save(category);
	}
	
	@Test
	public void testUpdateCategory() {
		categoryDto.setId(1);
		category.setId(1);
		
		// arrange
		when(categoryRepo.existsByName(categoryDto.getName())).thenReturn(false);
		when(mapper.map(categoryDto, Category.class)).thenReturn(category);
		when(categoryRepo.save(category)).thenReturn(category);
		
		// act
		Boolean saveCategory = categoryService.saveCategory(categoryDto);
		
		// assert
		assertTrue(saveCategory);
		
		// verify
		verify(validation).categoryValidation(categoryDto);
		verify(categoryRepo).existsByName(categoryDto.getName());
		verify(categoryRepo).save(category);
	}
	
	@Test
	public void testGetAllCategory() {
		when(categoryRepo.findByIsDeletedFalse()).thenReturn(categories);
		List<CategoryDto> allCategory = categoryService.getAllCategory();
		
		assertEquals(allCategory.size(), categories.size());
		verify(categoryRepo).findByIsDeletedFalse();
	}
	
	@Test
	public void testGetActiveCategory() {

	    // arrange
	    when(categoryRepo.findByIsActiveTrueAndIsDeletedFalse())
	            .thenReturn(categories);

	    CategoryResponse categoryResponse = new CategoryResponse();

	    when(mapper.map(category, CategoryResponse.class))
	            .thenReturn(categoryResponse);

	    // act
	    List<CategoryResponse> activeCategories =
	            categoryService.getActiveCategory();

	    // assert
	    assertNotNull(activeCategories);
	    assertEquals(1, activeCategories.size());
	    assertSame(categoryResponse, activeCategories.get(0));

	    // verify
	    verify(categoryRepo).findByIsActiveTrueAndIsDeletedFalse();
	    verify(mapper).map(category, CategoryResponse.class);
	}
	
	@Test
	public void testGetCategoryById() throws Exception {

	    // arrange
	    Integer id = 1;

	    category.setId(id);

	    when(categoryRepo.findByIdAndIsDeletedFalse(id))
	            .thenReturn(Optional.of(category));

	    when(mapper.map(category, CategoryDto.class))
	            .thenReturn(categoryDto);

	    // act
	    CategoryDto result = categoryService.getCategoryById(id);

	    // assert
	    assertNotNull(result);
	    assertEquals(categoryDto, result);

	    // verify
	    verify(categoryRepo).findByIdAndIsDeletedFalse(id);
	    verify(mapper).map(category, CategoryDto.class);
	}
	
	
	@Test
	public void testGetCategoryByIdNotFound() {

	    Integer id = 100;

	    when(categoryRepo.findByIdAndIsDeletedFalse(id))
	            .thenReturn(Optional.empty());

	    ResourceNotFoundException exception = assertThrows(
	            ResourceNotFoundException.class,
	            () -> categoryService.getCategoryById(id)
	    );

	    assertEquals(
	            "Category not found with id=" + id,
	            exception.getMessage()
	    );

	    verify(categoryRepo).findByIdAndIsDeletedFalse(id);

	    verify(mapper, never()).map(
	            any(),
	            eq(CategoryDto.class)
	    );
	}
	
	@Test
	public void testDeleteCategory() {

	    // arrange
	    Integer id = 1;

	    category.setId(id);
	    category.setIsDeleted(false);

	    when(categoryRepo.findById(id))
	            .thenReturn(Optional.of(category));

	    when(categoryRepo.save(category))
	            .thenReturn(category);

	    // act
	    Boolean result = categoryService.deleteCategory(id);

	    // assert
	    assertTrue(result);
	    assertTrue(category.getIsDeleted());

	    // verify
	    verify(categoryRepo).findById(id);
	    verify(categoryRepo).save(category);
	}
	
	@Test
	public void testDeleteCategoryNotFound() {

	    Integer id = 100;

	    when(categoryRepo.findById(id))
	            .thenReturn(Optional.empty());

	    Boolean result = categoryService.deleteCategory(id);

	    assertFalse(result);

	    verify(categoryRepo).findById(id);

	    verify(categoryRepo, never()).save(any(Category.class));
	}
}

