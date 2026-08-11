package com.exam_paper.backend.dto.lecturer;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacketDetailsResponseDTO {

    private String packetId;

    private String courseCode;

    private String courseName;

    private String departmentName;

    private Integer academicYear;

    private Integer semester;

    private LocalDate deadline;

    private String status;

    private String currentHolderName;

}