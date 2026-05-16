package com.studyhub.backend.repository;

import com.studyhub.backend.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByUserId(Long userId);
    Optional<Deck> findByClientIdAndUserId(String clientId, Long userId);
    boolean existsByClientIdAndUserId(String clientId, Long userId);
}