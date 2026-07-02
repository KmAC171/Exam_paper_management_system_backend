package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor

public class AddCommentDTO {

    private Long userId;
        private String commentText;
    }

