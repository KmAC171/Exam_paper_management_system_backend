package com.exam_paper.backend.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LecturerMarkingSummaryResponseDTO {

    private String lecturerId;

    private int totalScripts;

    private int markedScripts;

    private int remainingScripts;

}