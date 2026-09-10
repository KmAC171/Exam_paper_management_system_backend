package com.exam_paper.backend.dto.printing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotAvailabilityDTO {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeLabel;

    @JsonProperty("isAvailable")
    private boolean isAvailable;

    @JsonProperty("available")
    public boolean getAvailable() {
        return isAvailable;
    }

    private String location;
    private Long bookedScheduleId;
    private Long bookedPacketId;
    private String bookedCourseCode;
    private String bookedCourseName;
    private String bookedLecturerName;
}
