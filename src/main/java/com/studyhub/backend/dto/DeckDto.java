package com.studyhub.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DeckDto {
    private Long id;
    private String clientId;
    private String name;
    private List<CardDto> cards;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<CardDto> getCards() { return cards; }
    public void setCards(List<CardDto> cards) { this.cards = cards; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}