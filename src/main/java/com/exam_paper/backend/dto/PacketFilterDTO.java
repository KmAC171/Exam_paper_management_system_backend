package com.exam_paper.backend.dto;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketFilterDTO {


    private String status;


    private Long cycleId;


    private Long lecturerId;


    private Long moderatorId;

}