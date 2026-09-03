package com.exam_paper.backend.dto.lecturer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPacketFilterDTO {
    private String status;
    private String academicYear;
    private Integer semester;
    private String search;
}
