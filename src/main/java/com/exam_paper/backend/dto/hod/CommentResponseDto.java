package com.exam_paper.backend.dto.hod;

import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentResponseDto {
    private String commentId;
    private String packetId;
    private String userName;
    private String commentText;
    private LocalDateTime timestamp;
}
