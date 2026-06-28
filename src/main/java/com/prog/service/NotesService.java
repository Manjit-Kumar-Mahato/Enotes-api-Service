package com.prog.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.prog.dto.NotesDto;
import com.prog.entity.FileDetails;

public interface NotesService {

	public Boolean saveNotes(String notes,MultipartFile file) throws Exception;
	
	public List<NotesDto> getAllNotes();

	public byte[] downloadFile(FileDetails fileDtls) throws Exception;

	public FileDetails getFileDetails(Integer id) throws Exception;

}