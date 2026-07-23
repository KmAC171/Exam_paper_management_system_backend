package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponseDTO {

    private Long commentId;
    private String userName;
    private String commentText;
    private LocalDateTime timestamp;
}