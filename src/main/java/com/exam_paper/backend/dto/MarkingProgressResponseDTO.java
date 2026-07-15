package com.exam_paper.backend.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarkingProgressResponseDTO {


    private Long lecturerId;

    private String lecturerName;

    private Integer totalScripts;

    private Integer markedScripts;

    private Double progressPercentage;

}