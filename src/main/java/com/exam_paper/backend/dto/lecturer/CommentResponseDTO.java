package com.exam_paper.backend.dto.lecturer;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {
    private String commentId;
    private String packetId;
    private String userId;
    private String userName;
    private String commentText;
    private LocalDateTime timestamp;
}
