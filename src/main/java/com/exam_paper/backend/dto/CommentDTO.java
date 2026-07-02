package com.exam_paper.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Long commentId;

    private Long packetId;

    private Long userId;

    private String userName;

    private String commentText;

    private LocalDateTime timestamp;
}