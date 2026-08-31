package com.exam_paper.backend.dto;

import lombok.Data;

@Data
public class StatusUpdateDTO {
    private String action; // APPROVE, RETURN, REJECT
    private String note;   // optional note
}