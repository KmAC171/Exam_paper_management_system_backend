package com.exam_paper.backend.dto.hod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private String commentId;
    private String packetId;
    private String userName;
    private String commentText;
    private LocalDateTime timestamp;
}