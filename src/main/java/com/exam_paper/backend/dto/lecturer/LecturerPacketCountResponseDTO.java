package com.exam_paper.backend.dto.lecturer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerPacketCountResponseDTO {
    private String lecturerId;
    private long assignedPacketCount;
}
