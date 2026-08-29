package com.exam_paper.backend.dto.hod;

import lombok.Data;

@Data
public class CommentRequestDto {
    private String packetId;
    private String userId; // HOD User ID
    private String commentText;
}
