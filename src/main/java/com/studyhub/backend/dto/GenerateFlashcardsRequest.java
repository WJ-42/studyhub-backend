package com.studyhub.backend.dto;

public class GenerateFlashcardsRequest {
    private String text;

    public GenerateFlashcardsRequest() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}