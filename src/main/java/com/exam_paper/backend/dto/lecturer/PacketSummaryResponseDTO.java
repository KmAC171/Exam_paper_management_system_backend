package com.exam_paper.backend.dto.lecturer;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketSummaryResponseDTO {
    private String packetId;
    private String courseCode;
    private String courseName;
    private String status;
    private LocalDate deadline;
    private String currentHolderName;
}
