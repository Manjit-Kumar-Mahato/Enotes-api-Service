package com.prog.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.prog.dto.TodoDto;
import com.prog.dto.TodoDto.StatusDto;
import com.prog.entity.Todo;
import com.prog.enums.TodoStatus;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.TodoRepository;
import com.prog.service.TodoService;
import com.prog.util.Validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
	
	private final TodoRepository todoRepo;
	
	private final ModelMapper mapper;
	
	private final Validation validation;

	@Override
	public Boolean saveTodo(TodoDto todoDto) throws Exception {

		log.info("TodoServiceImpl : saveTodo() : Saving todo");

		validation.todoValidation(todoDto);

		Todo todo = mapper.map(todoDto, Todo.class);
		todo.setStatusId(todoDto.getStatus().getId());

		Todo saveTodo = todoRepo.save(todo);

		if (!ObjectUtils.isEmpty(saveTodo)) {
			log.info("TodoServiceImpl : saveTodo() : Todo saved successfully. Id={}", saveTodo.getId());
			return true;
		}

		log.error("TodoServiceImpl : saveTodo() : Failed to save todo");

		return false;
	}

	@Override
	public TodoDto getTodoById(Integer id) throws Exception {

		log.info("TodoServiceImpl : getTodoById() : Fetching todo. Id={}", id);

		Todo todo = todoRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not Found | Id Invalid"));

		TodoDto todoDto = mapper.map(todo, TodoDto.class);

		setStatus(todoDto, todo);

		log.info("TodoServiceImpl : getTodoById() : Todo fetched successfully. Id={}", id);

		return todoDto;
	}

	private void setStatus(TodoDto todoDto, Todo todo) {

		for (TodoStatus st : TodoStatus.values()) {
			if (st.getId().equals(todo.getStatusId())) {
				StatusDto statusDto = StatusDto.builder()
						.id(st.getId())
						.name(st.getName())
						.build();
				todoDto.setStatus(statusDto);
			}
		}
	}

	@Override
	public List<TodoDto> getTodoByUser() {

		Integer userId = 2;

		log.info("TodoServiceImpl : getTodoByUser() : Fetching todos for userId={}", userId);

		List<Todo> todos = todoRepo.findByCreatedBy(userId);

		log.info("TodoServiceImpl : getTodoByUser() : {} todos fetched", todos.size());

		return todos.stream()
				.map(td -> mapper.map(td, TodoDto.class))
				.toList();
	}

}