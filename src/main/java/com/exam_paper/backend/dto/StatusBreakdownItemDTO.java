package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusBreakdownItemDTO {
    private String key;
    private String name;
    private long count;
    private double percentage;
    private String color;
    private String bg;
    private int order;
}
