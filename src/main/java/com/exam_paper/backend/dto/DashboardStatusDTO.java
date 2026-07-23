package com.exam_paper.backend.dto;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatusDTO {


    private Long pending;


    private Long completed;


    private Long inProgress;


    private Long overdue;


}