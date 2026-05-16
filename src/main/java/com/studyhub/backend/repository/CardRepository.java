package com.studyhub.backend.repository;

import com.studyhub.backend.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByDeckId(Long deckId);
    Optional<Card> findByClientIdAndDeckId(String clientId, Long deckId);
    void deleteByDeckId(Long deckId);
}