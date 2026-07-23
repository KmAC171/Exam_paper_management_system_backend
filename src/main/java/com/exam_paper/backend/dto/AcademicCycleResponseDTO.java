package com.exam_paper.backend.dto;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicCycleResponseDTO {


    private Long cycleId;


    private Integer year;


    private Integer semester;


    private String status;

}