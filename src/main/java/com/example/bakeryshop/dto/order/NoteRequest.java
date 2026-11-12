package com.example.bakeryshop.dto.order;

import jakarta.validation.constraints.Size;

// Represents a note request payload.
public record NoteRequest(
        @Size(max = 200, message = "Note must be 200 characters or less")
        String note) {
}