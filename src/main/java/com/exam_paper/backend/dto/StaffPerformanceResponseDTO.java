package com.exam_paper.backend.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffPerformanceResponseDTO {


    private Long userId;

    private String staffName;

    private String role;

    private Long completedPackets;

    private Long pendingPackets;

    private Long overduePackets;

}