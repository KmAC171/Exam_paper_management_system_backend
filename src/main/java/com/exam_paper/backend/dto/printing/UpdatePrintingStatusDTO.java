package com.exam_paper.backend.dto.printing;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePrintingStatusDTO {
    private String status; // IN_PROGRESS, COMPLETED, DELAYED, CANCELLED
    private String notes;
}
