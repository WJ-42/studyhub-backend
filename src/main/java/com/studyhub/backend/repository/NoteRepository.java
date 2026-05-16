package com.studyhub.backend.repository;

import com.studyhub.backend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserId(Long userId);
    Optional<Note> findByClientIdAndUserId(String clientId, Long userId);
    boolean existsByClientIdAndUserId(String clientId, Long userId);
}