package com.exam_paper.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HodNotifyStaffDTO {
    private Long targetUserId;
    private String message;
    private String title;
    private boolean isUrgent;
    private String courseCode;
    private Long packetId;
}
