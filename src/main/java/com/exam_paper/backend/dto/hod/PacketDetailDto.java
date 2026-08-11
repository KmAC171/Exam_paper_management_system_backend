package com.exam_paper.backend.dto.hod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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