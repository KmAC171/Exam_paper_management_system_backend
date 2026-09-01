package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String comment;
    private String authorName;
    private String authorInitials;
    private String authorColor;
    private String createdAt;
}