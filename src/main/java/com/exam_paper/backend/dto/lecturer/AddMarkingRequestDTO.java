package com.exam_paper.backend.dto.lecturer;


import lombok.*;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMarkingRequestDTO {


    private String packetId;


    private String lecturerId;


    private Integer totalScripts;


}