package com.exam_paper.backend.dto.lecturer;


import lombok.*;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketSummaryResponseDTO {


    private long totalPackets;


    private long pendingPackets;


    private long inProgressPackets;


    private long completedPackets;


    private long overduePackets;

}