package com.exam_paper.backend.dto.printing;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleSlotRequestDTO {
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private Integer copies;
    private String notes;
}
