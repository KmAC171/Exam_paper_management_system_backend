package com.exam_paper.backend.dto.hod;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PacketDetailDto {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String departmentName;
    private String cycleId;
    private String status;
    private LocalDate deadline;
    private String currentHolderName;
    private String lastUpdatedUser;
    private List<PacketMovementDto> movementHistory;
}
