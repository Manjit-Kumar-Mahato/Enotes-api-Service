package com.prog.service;

import java.util.List;

import com.prog.dto.CategoryDto;
import com.prog.dto.CategoryReponse;

public interface CategoryService {

	public Boolean saveCategory(CategoryDto categoryDto);
	
	public List<CategoryDto> getAllCategory();

	public List<CategoryReponse> getActiveCategory();

	public CategoryDto getCategoryById(Integer id);

	public Boolean deleteCategory(Integer id);

}