package com.exam_paper.backend.dto;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HodPacketResponseDTO {


    private Long packetId;

    private String courseCode;

    private String courseName;

    private String status;

    private LocalDate deadline;

    private String currentHolder;

    private Integer semester;

    private Integer year;

}