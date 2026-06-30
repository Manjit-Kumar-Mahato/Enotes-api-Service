package com.prog.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.prog.dto.FavouriteNoteDto;
import com.prog.dto.NotesDto;
import com.prog.dto.NotesResponse;
import com.prog.entity.FileDetails;

public interface NotesService {

	public Boolean saveNotes(String notes,MultipartFile file) throws Exception;
	
	public List<NotesDto> getAllNotes();

	public byte[] downloadFile(FileDetails fileDtls) throws Exception;

	public FileDetails getFileDetails(Integer id) throws Exception;

	public NotesResponse getAllNotesByUser(Integer userId, Integer pageNo, Integer pageSize);

	public void softDeleteNotes(Integer id) throws Exception;

	public void restoreNotes(Integer id) throws Exception;

	public List<NotesDto> getUserRecycleBinNotes(Integer userId);

	public void hardDeleteNotes(Integer id) throws Exception;

	public void emptyRecycleBin(int userId);
	
	public void favoriteNotes(Integer noteId) throws Exception;
	
	public void unFavoriteNotes(Integer noteId) throws Exception;

	public List<FavouriteNoteDto> getUserFavoriteNotes() throws Exception;
	
	public Boolean copyNotes(Integer id) throws Exception; 

}