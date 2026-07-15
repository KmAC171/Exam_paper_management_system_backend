package com.exam_paper.backend.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {


    private String reportType;

    private Long totalPackets;

    private Long completedPackets;

    private Long pendingPackets;

    private Long overduePackets;

    private String generatedDate;

}