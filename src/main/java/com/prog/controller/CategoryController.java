package com.prog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog.dto.CategoryDto;
import com.prog.dto.CategoryResponse;
import com.prog.endpoint.CategoryEndpoint;
import com.prog.service.CategoryService;
import com.prog.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CategoryController implements CategoryEndpoint{

	@Autowired
	private CategoryService categoryService;

	@Override
	public ResponseEntity<?> saveCategory(@RequestBody CategoryDto categoryDto) {
		log.info("CategoryController : saveCategory() : Save category request received. Category={}",categoryDto.getName());
		Boolean saveCategory = categoryService.saveCategory(categoryDto);
		if (saveCategory) {
			log.info("CategoryController : saveCategory() : Category saved successfully. Category={}",categoryDto.getName());
			return CommonUtil.createBuildResponseMessage("saved success", HttpStatus.CREATED);
		}
		log.error("CategoryController : saveCategory() : Failed to save category. Category={}",categoryDto.getName());
		return CommonUtil.createErrorResponseMessage("Category Not saved", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<?> getAllCategory() {
		log.info("CategoryController : getAllCategory() : Fetch all categories request received");
		List<CategoryDto> allCategory = categoryService.getAllCategory();
		if (CollectionUtils.isEmpty(allCategory)) {
			log.warn("CategoryController : getAllCategory() : No categories found");
			return ResponseEntity.noContent().build();
		}
		log.info("CategoryController : getAllCategory() : {} categories fetched successfully",allCategory.size());
		return CommonUtil.createBuildResponse(allCategory, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getActiveCategory() {
		log.info("CategoryController : getActiveCategory() : Fetch active categories request received");
		List<CategoryResponse> allCategory = categoryService.getActiveCategory();
		if (CollectionUtils.isEmpty(allCategory)) {
			log.warn("CategoryController : getActiveCategory() : No active categories found");
			return ResponseEntity.noContent().build();
		}
		log.info("CategoryController : getActiveCategory() : {} active categories fetched successfully",allCategory.size());
		return CommonUtil.createBuildResponse(allCategory, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCategortDetailsById(@PathVariable Integer id) throws Exception {
		log.info("CategoryController : getCategoryDetailsById() : Fetch category request. Id={}", id);
		CategoryDto categoryDto = categoryService.getCategoryById(id);
		if (ObjectUtils.isEmpty(categoryDto)) {
			log.warn("CategoryController : getCategoryDetailsById() : Category not found. Id={}", id);
			return CommonUtil.createErrorResponseMessage("Internal Server Error", HttpStatus.NOT_FOUND);
		}
		log.info("CategoryController : getCategoryDetailsById() : Category fetched successfully. Id={}", id);
		return CommonUtil.createBuildResponse(categoryDto, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteCategoryById(@PathVariable Integer id) {
		log.info("CategoryController : deleteCategoryById() : Delete category request. Id={}", id);
		Boolean deleted = categoryService.deleteCategory(id);
		if (deleted) {
			log.info("CategoryController : deleteCategoryById() : Category deleted successfully. Id={}", id);
			return CommonUtil.createBuildResponse("Category deleted success", HttpStatus.OK);
		}
		log.error("CategoryController : deleteCategoryById() : Failed to delete category. Id={}", id);
		return CommonUtil.createErrorResponseMessage("Category Not deleted", HttpStatus.INTERNAL_SERVER_ERROR);
	}

}