package com.prog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prog.entity.Notes;

public interface NotesRepository extends JpaRepository<Notes, Integer>{

}