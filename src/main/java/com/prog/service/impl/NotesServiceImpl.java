package com.prog.service.impl;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.prog.dto.NotesDto;
import com.prog.dto.NotesDto.CategoryDto;
import com.prog.entity.Category;
import com.prog.entity.Notes;
import com.prog.exception.ResourceNotFoundException;
import com.prog.repository.CategoryRepository;
import com.prog.repository.NotesRepository;
import com.prog.service.NotesService;

@Service
public class NotesServiceImpl implements NotesService {

	@Autowired
	private NotesRepository notesRepo;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private CategoryRepository categoryRepo;

	@Override
	public Boolean saveNotes(NotesDto notesDto) throws Exception {

		// category validation
		checkCategoryExist(notesDto.getCategory());

		Notes notes = mapper.map(notesDto, Notes.class);
		Notes saveNotes = notesRepo.save(notes);
		if (!ObjectUtils.isEmpty(saveNotes)) {
			return true;
		}
		return false;
	}

	private void checkCategoryExist(CategoryDto category) throws Exception {
		categoryRepo.findById(category.getId()).orElseThrow(() -> new ResourceNotFoundException("category id invalid"));
	}

	@Override
	public List<NotesDto> getAllNotes() {
		return notesRepo.findAll().stream().map(note -> mapper.map(note, NotesDto.class)).toList();
	}

}