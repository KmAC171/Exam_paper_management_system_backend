package com.exam_paper.backend.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadResponseDTO {


    private Long userId;

    private String staffName;

    private String role;

    private Long totalPackets;

}