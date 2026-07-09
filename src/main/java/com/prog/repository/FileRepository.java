package com.prog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prog.entity.FileDetails;

public interface FileRepository extends JpaRepository<FileDetails, Integer> {

}