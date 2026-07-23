package com.exam_paper.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class MarkingResponseDTO {


    private Long markingId;


    private Long packetId;


    private Integer scriptCount;


    private Integer completedScripts;


    private LocalDateTime createdAt;


}