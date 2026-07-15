package com.exam_paper.backend.dto;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacketDetailsResponseDTO {


    private Long packetId;

    private String courseCode;

    private String courseName;

    private String departmentName;

    private Integer year;

    private Integer semester;

    private String status;

    private LocalDate deadline;

    private String currentHolder;

    private String assignedLecturer;

    private String assignedModerator;

}