package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class AttachmentDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String fileSize;
    private String uploadedBy;
    private String uploadedAt;
    private String downloadUrl;
}