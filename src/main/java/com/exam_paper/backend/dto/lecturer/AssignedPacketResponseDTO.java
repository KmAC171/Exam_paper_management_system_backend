package com.exam_paper.backend.dto.lecturer;

import com.exam_paper.backend.enums.TaskType;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedPacketResponseDTO {

    private String packetId;

    private String courseCode;

    private String courseName;

    private String departmentName;

    private Integer academicYear;

    private Integer semester;

    private LocalDate deadline;

    private String status;

    private String currentHolderName;

    private TaskType taskType;
}