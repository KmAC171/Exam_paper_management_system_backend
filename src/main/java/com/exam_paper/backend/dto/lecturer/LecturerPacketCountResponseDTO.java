package com.exam_paper.backend.dto.lecturer;



import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LecturerPacketCountResponseDTO {

    private String lecturerId;

    private long assignedPacketCount;

}