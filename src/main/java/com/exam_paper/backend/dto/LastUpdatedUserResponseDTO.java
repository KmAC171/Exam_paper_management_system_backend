package com.exam_paper.backend.dto;


import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LastUpdatedUserResponseDTO {


    private Long userId;


    private String userName;


    private String action;


    private LocalDateTime updatedAt;


}