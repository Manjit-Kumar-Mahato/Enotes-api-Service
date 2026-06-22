package com.prog.service;

import java.util.List;

import com.prog.entity.Category;

public interface CategoryService {

	public Boolean saveCategory(Category category);
	
	public List<Category> getAllCategory();

}