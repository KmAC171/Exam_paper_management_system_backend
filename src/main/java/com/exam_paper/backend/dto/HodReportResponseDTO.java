package com.exam_paper.backend.dto;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HodReportResponseDTO {


    private Long totalPackets;


    private Long completedPackets;


    private Long pendingPackets;


    private Long inProgressPackets;


    private Long overduePackets;


}