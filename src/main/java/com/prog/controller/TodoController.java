package com.prog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import com.prog.dto.TodoDto;
import com.prog.endpoint.TodoEndpoint;
import com.prog.service.TodoService;
import com.prog.util.CommonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TodoController implements TodoEndpoint{

	private final TodoService todoService;

	@Override
	public ResponseEntity<?> saveTodo(TodoDto todo) throws Exception {
		log.info("TodoController : saveTodo() : Save todo request received");
		Boolean saveTodo = todoService.saveTodo(todo);
		if (saveTodo) {
			log.info("TodoController : saveTodo() : Todo saved successfully");
			return CommonUtil.createBuildResponseMessage("Todo Saved Success", HttpStatus.CREATED);
		}
		log.error("TodoController : saveTodo() : Failed to save todo");
		return CommonUtil.createErrorResponseMessage("Todo not save", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<?> getTodoById(Integer id) throws Exception {
		log.info("TodoController : getTodoById() : Fetch todo request received. TodoId={}", id);
		TodoDto todo = todoService.getTodoById(id);
		log.info("TodoController : getTodoById() : Todo fetched successfully. TodoId={}", id);
		return CommonUtil.createBuildResponse(todo, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllTodoByUser() throws Exception {
		log.info("TodoController : getAllTodoByUser() : Fetch user todo list request received");
		List<TodoDto> todoList = todoService.getTodoByUser();
		if (CollectionUtils.isEmpty(todoList)) {
			log.warn("TodoController : getAllTodoByUser() : No todo found for user");
			return ResponseEntity.noContent().build();
		}
		log.info("TodoController : getAllTodoByUser() : {} todos fetched successfully", todoList.size());
		return CommonUtil.createBuildResponse(todoList, HttpStatus.OK);
	}


}